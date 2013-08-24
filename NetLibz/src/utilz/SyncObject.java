/*
 LionChat Server/Client library program
 Copyright (C) 2013  Enrico Fasoli

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
 */

package utilz;

import java.io.Serializable;

/**
 *
 * @author fazo
 */
public class SyncObject implements Serializable{
    /*questo è un oggetto vuoto serializzabile. viene utilizzato per mantenere viva
     la connessione con i client spedendo continuamente pacchetti per dire al client
     "sono il server, ci sono e stiamo comunicando" in questo modo, quando non arrivano
     più per un certo tempo, il client è certo che la connessione è stata persa.
     ogni 300ms il server invia un SyncObject a ogni client.*/
     
}
