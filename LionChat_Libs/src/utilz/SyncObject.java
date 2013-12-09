package utilz;

import java.io.Serializable;

/**
 * Questo è un oggetto vuoto serializzabile, viene utilizzato per mantenere viva
 * la connessione con i client spedendo continuamente pacchetti per dire al
 * client "sono il server, ci sono e stiamo comunicando" in questo modo, quando
 * non arrivano più per un certo tempo, il client è certo che la connessione è
 * stata persa.
 *
 * @author fazo
 */
public class SyncObject implements Serializable {
}
