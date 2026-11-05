    package com.calculadora.servicio;

    import java.awt.image.BufferedImage;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.util.concurrent.*;
    import javax.imageio.ImageIO;

    /** Un trabajo activo, una petición pendiente y una sola imagen en memoria. */
    public final class GestorGraficas implements AutoCloseable {
        public record GraficaPreparada(ParametrosGrafica parametros, BufferedImage imagen,
                                      Path archivo, int serieId, boolean indefinidos) { }

        @FunctionalInterface
        interface Generador {
            GraficaPreparada generar(ParametrosGrafica parametros) throws Exception;
        }


        private final ScheduledThreadPoolExecutor trabajador;
        private final Generador generador;
        private Solicitud enCurso;
        private Solicitud pendiente;
        private GraficaPreparada ultima;
        private boolean cerrado;

        private static final class Solicitud {
            final ParametrosGrafica parametros;
            final CompletableFuture<GraficaPreparada> resultado = new CompletableFuture<>();
            ScheduledFuture<?> tarea;

            Solicitud(ParametrosGrafica parametros) { this.parametros = parametros; }
        }

        public GestorGraficas() { this(GestorGraficas::generar); }

        // Permite probar la cola y la reutilización sin Python ni base de datos.
        GestorGraficas(Generador generador) {
            this.generador = generador;
            trabajador = new ScheduledThreadPoolExecutor(1, tarea -> {
                Thread hilo = new Thread(tarea, "preparacion-graficas");
                hilo.setDaemon(true);
                return hilo;
            });
            trabajador.setRemoveOnCancelPolicy(true);
        }

        public synchronized CompletableFuture<GraficaPreparada> preparar(ParametrosGrafica parametros) {
            if (cerrado) return CompletableFuture.failedFuture(new IllegalStateException("El preparador está cerrado."));
            if (ultima != null && ultima.parametros().equals(parametros)) {
                descartarPendiente();
                return CompletableFuture.completedFuture(ultima);
            }
            if (enCurso != null && enCurso.parametros.equals(parametros)) {
                descartarPendiente();
                return enCurso.resultado;
            }
            if (pendiente != null && pendiente.parametros.equals(parametros)) return pendiente.resultado;
            descartarPendiente();
            Solicitud solicitud = new Solicitud(parametros);
            pendiente = solicitud;
            // Agrupa cambios rápidos sin iniciar un proceso Python por cada pulsación.
            solicitud.tarea = trabajador.schedule(() -> ejecutar(solicitud), 250, TimeUnit.MILLISECONDS);
            return solicitud.resultado;
        }

        private void ejecutar(Solicitud solicitud) {
            synchronized (this) {
                if (cerrado || pendiente != solicitud) return;
                pendiente = null;
                enCurso = solicitud;
            }
            GraficaPreparada grafica = null;
            Exception fallo = null;
            try { grafica = generador.generar(solicitud.parametros); }
            catch (Exception ex) { fallo = ex; }
            synchronized (this) {
                enCurso = null;
                if (grafica != null && !cerrado) ultima = grafica;
            }
            if (fallo == null) solicitud.resultado.complete(grafica);
            else solicitud.resultado.completeExceptionally(fallo);
        }

        private void descartarPendiente() {
            if (pendiente != null) {
                Solicitud descartada = pendiente;
                pendiente = null;
                descartada.tarea.cancel(false);
                descartada.resultado.cancel(false);
            }
        }

        @Override public synchronized void close() {
            cerrado = true;
            descartarPendiente();
            ultima = null;
            // Una transacción ya iniciada termina normalmente; no se interrumpe a mitad.
            trabajador.shutdown();



        }

        private static GraficaPreparada generar(ParametrosGrafica p) throws Exception {
            SerieService servicio = new SerieServiceImpl();
            int id = servicio.generarSerie(p.funcion(), p.minimo(), p.maximo(), p.paso(), p.modo());
            try {
                Path carpeta = Path.of("..", "graficas").toAbsolutePath().normalize();
                Files.createDirectories(carpeta);
                Path png = Files.createTempFile(carpeta, "serie-" + id + "-", ".png");
                if (!servicio.generarGrafica(id, png.toString())) {
                    throw new IllegalStateException("Python no confirmó la creación de la imagen.");
                }
                BufferedImage imagen = ImageIO.read(png.toFile());
                if (imagen == null) throw new IllegalStateException("No se puede leer la imagen generada.");
                return new GraficaPreparada(p, imagen, png, id, servicio.tienePuntosIndefinidos(id));
            } catch (Exception ex) {
                throw new Exception("La serie #" + id + " quedó guardada, pero no se pudo preparar la gráfica: " + ex.getMessage(), ex);
            }



        }
    }
