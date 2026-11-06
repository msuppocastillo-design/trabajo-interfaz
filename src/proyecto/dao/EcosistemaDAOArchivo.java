package dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.Clima;
import modelo.Conejo;
import modelo.Ecosistema;
import modelo.Entidad;
import modelo.Lobo;
import modelo.Planta;

/**
 * DAO que guarda la simulación en un archivo de texto plano (ecosistema.txt),
 * en la carpeta del proyecto (ruta relativa). Es la ÚNICA clase que toca el archivo.
 * Conoce al modelo, pero no a la vista ni al controlador, y no usa Swing.
 *
 * Formato (secciones; en la cabecera, una línea clave=valor):
 *
 *   clima=SOLEADO
 *   turnosTotales=20
 *   ...
 *   [ENTIDADES]   Tipo;nombre;energia;edad;viva;datos propios del tipo
 *   [HISTORIAL]   turno;plantas;conejos;lobos
 *   [EVENTOS]     un evento por línea (los del último turno)
 */
public class EcosistemaDAOArchivo implements EcosistemaDAO {

    private static final String ARCHIVO = "ecosistema.txt";
    private static final String SEP = ";";
    private static final String SEC_ENTIDADES = "[ENTIDADES]";
    private static final String SEC_HISTORIAL = "[HISTORIAL]";
    private static final String SEC_EVENTOS = "[EVENTOS]";

    //  LEER: archivo → Ecosistema
    @Override
    public Ecosistema cargar() throws DAOException {
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) {
            return null;   // primera vez: no hay nada guardado
        }

        Map<String, String> cabecera = new HashMap<>();
        List<String> entidades = new ArrayList<>();
        List<String> historial = new ArrayList<>();
        List<String> eventos = new ArrayList<>();

        try (BufferedReader lector = Files.newBufferedReader(archivo.toPath(), StandardCharsets.UTF_8)) {
            List<String> seccionActual = null;
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                switch (linea.trim()) {
                    case SEC_ENTIDADES: seccionActual = entidades; continue;
                    case SEC_HISTORIAL: seccionActual = historial; continue;
                    case SEC_EVENTOS:   seccionActual = eventos;   continue;
                    default: break;
                }
                if (seccionActual != null) {
                    seccionActual.add(linea);
                } else {
                    String[] kv = linea.split("=", 2);
                    if (kv.length != 2) {
                        throw new DAOException("Línea inválida en la cabecera: " + linea);
                    }
                    cabecera.put(kv[0].trim(), kv[1]);
                }
            }
        } catch (IOException e) {
            throw new DAOException("No se pudo leer el archivo " + ARCHIVO + ": " + e.getMessage(), e);
        }

        return armarEcosistema(cabecera, entidades, historial, eventos);
    }

    private Ecosistema armarEcosistema(Map<String, String> cab, List<String> entidades,
                                       List<String> historial, List<String> eventos) throws DAOException {
        Clima clima;
        try {
            clima = Clima.valueOf(texto(cab, "clima"));
        } catch (IllegalArgumentException e) {
            throw new DAOException("Clima desconocido en el archivo: " + cab.get("clima"));
        }
        int turnosTotales = entero(cab, "turnosTotales");
        if (turnosTotales <= 0) {
            throw new DAOException("La cantidad de turnos totales del archivo no es válida.");
        }
        Ecosistema eco = new Ecosistema(clima, turnosTotales);

        int turno = entero(cab, "turnoActual");
        if (turno < 0) {
            throw new DAOException("El turno actual del archivo no es válido.");
        }
        eco.restaurarEstado(turno, entero(cab, "lobosAgregadosHistorico"),
                Boolean.parseBoolean(texto(cab, "terminada")),
                cab.getOrDefault("causaFin", ""),
                Boolean.parseBoolean(texto(cab, "intervencionUsada")),
                entero(cab, "turnoMayorActividad"), entero(cab, "cantidadEventosMayorActividad"),
                entero(cab, "contadorPlanta"), entero(cab, "contadorConejo"), entero(cab, "contadorLobo"));

        int[] nac = tres(cab, "nacimientos");
        int[] mue = tres(cab, "muertes");
        eco.restaurarContadorEstadistico("Planta", nac[0], mue[0]);
        eco.restaurarContadorEstadistico("Conejo", nac[1], mue[1]);
        eco.restaurarContadorEstadistico("Lobo", nac[2], mue[2]);

        for (String linea : entidades) {
            eco.restaurarEntidad(entidadDeTexto(linea));
        }
        for (String linea : historial) {
            String[] p = linea.split(SEP);
            if (p.length != 4) {
                throw new DAOException("Línea de historial inválida: " + linea);
            }
            try {
                eco.restaurarHistorial(new int[]{Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()),
                    Integer.parseInt(p[2].trim()), Integer.parseInt(p[3].trim())});
            } catch (NumberFormatException e) {
                throw new DAOException("Línea de historial inválida: " + linea);
            }
        }
        eco.restaurarEventos(eventos);
        return eco;
    }

    //  ESCRIBIR: Ecosistema → archivo
    @Override
    public boolean guardar(Ecosistema eco) {
        try (BufferedWriter w = Files.newBufferedWriter(new File(ARCHIVO).toPath(), StandardCharsets.UTF_8)) {
            linea(w, "clima=" + eco.getClimaActual().name());
            linea(w, "turnosTotales=" + eco.getTurnosTotales());
            linea(w, "turnoActual=" + eco.getTurnoActual());
            linea(w, "lobosAgregadosHistorico=" + eco.getLobosAgregadosHistorico());
            linea(w, "terminada=" + eco.isTerminada());
            linea(w, "causaFin=" + eco.getCausaFin());
            linea(w, "intervencionUsada=" + eco.isIntervencionUsada());
            linea(w, "turnoMayorActividad=" + eco.getTurnoDeMayorActividad());
            linea(w, "cantidadEventosMayorActividad=" + eco.getCantidadEventosMayorActividad());
            linea(w, "contadorPlanta=" + eco.getContadorPlanta());
            linea(w, "contadorConejo=" + eco.getContadorConejo());
            linea(w, "contadorLobo=" + eco.getContadorLobo());
            linea(w, "nacimientos=" + eco.getNacimientos("Planta") + SEP + eco.getNacimientos("Conejo")
                    + SEP + eco.getNacimientos("Lobo"));
            linea(w, "muertes=" + eco.getMuertes("Planta") + SEP + eco.getMuertes("Conejo")
                    + SEP + eco.getMuertes("Lobo"));

            linea(w, SEC_ENTIDADES);   // también se guardan las muertas: las necesita el reporte
            for (Planta p : eco.getPlantas()) linea(w, entidadATexto(p));
            for (Conejo c : eco.getConejos()) linea(w, entidadATexto(c));
            for (Lobo l : eco.getLobos()) linea(w, entidadATexto(l));

            linea(w, SEC_HISTORIAL);
            for (int[] f : eco.getHistorialPoblaciones()) {
                linea(w, f[0] + SEP + f[1] + SEP + f[2] + SEP + f[3]);
            }

            linea(w, SEC_EVENTOS);
            for (String e : eco.getEventosTurno()) linea(w, e.replace("\n", " "));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean eliminar() {
        File archivo = new File(ARCHIVO);
        return !archivo.exists() || archivo.delete();
    }

    //  Conversión entidad ⇄ texto (privados)

    private String entidadATexto(Entidad e) {
        String base = e.getTipo() + SEP + e.getNombre() + SEP + e.getEnergia() + SEP + e.getEdad() + SEP + e.isViva();
        if (e instanceof Planta) {
            return base + SEP + ((Planta) e).getTamanio();
        } else if (e instanceof Conejo) {
            Conejo c = (Conejo) e;
            return base + SEP + c.getVelocidad() + SEP + c.getPeso();
        } else {
            Lobo l = (Lobo) e;
            return base + SEP + l.getVelocidad() + SEP + l.getPeso() + SEP + l.getExitosCaza();
        }
    }

    private Entidad entidadDeTexto(String linea) throws DAOException {
        String[] p = linea.split(SEP);
        try {
            String tipo = p[0];
            String nombre = p[1];
            double energia = Double.parseDouble(p[2]);
            int edad = Integer.parseInt(p[3]);
            boolean viva = Boolean.parseBoolean(p[4]);
            if (nombre.trim().isEmpty() || energia < 0 || edad < 0) {
                throw new DAOException("Entidad con datos inválidos: " + linea);
            }
            Entidad e;
            switch (tipo) {
                case "Planta":
                    e = new Planta(nombre, energia, Integer.parseInt(p[5]));
                    break;
                case "Conejo":
                    e = new Conejo(nombre, energia, Integer.parseInt(p[5]), Double.parseDouble(p[6]));
                    break;
                case "Lobo":
                    Lobo l = new Lobo(nombre, energia, Integer.parseInt(p[5]), Double.parseDouble(p[6]));
                    l.setExitosCaza(Integer.parseInt(p[7]));
                    e = l;
                    break;
                default:
                    throw new DAOException("Tipo de entidad desconocido: " + tipo);
            }
            e.setEdad(edad);
            e.setViva(viva);
            return e;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            throw new DAOException("Línea de entidad inválida: " + linea);
        }
    }

    // utilidades de lectura de la cabecera 

    private String texto(Map<String, String> cab, String clave) throws DAOException {
        String v = cab.get(clave);
        if (v == null) {
            throw new DAOException("Falta el dato '" + clave + "' en el archivo.");
        }
        return v.trim();
    }

    private int entero(Map<String, String> cab, String clave) throws DAOException {
        try {
            return Integer.parseInt(texto(cab, clave));
        } catch (NumberFormatException e) {
            throw new DAOException("El dato '" + clave + "' no es un número válido.");
        }
    }

    private int[] tres(Map<String, String> cab, String clave) throws DAOException {
        String[] p = texto(cab, clave).split(SEP);
        if (p.length != 3) {
            throw new DAOException("El dato '" + clave + "' debe tener 3 valores.");
        }
        try {
            return new int[]{Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim())};
        } catch (NumberFormatException e) {
            throw new DAOException("El dato '" + clave + "' no es válido.");
        }
    }

    private void linea(BufferedWriter w, String texto) throws IOException {
        w.write(texto);
        w.newLine();
    }
}
