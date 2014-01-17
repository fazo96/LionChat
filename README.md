# LionChat
Chat client/server basata su java con le seguenti features:
- Gestione di permessi e gruppi a cui un utente puo' appartenere.
- Gestione di più canali in modo simile a IRC
- Le password sono al sicuro: il client non salva mai su file le password degli utenti e non le invia mai via rete, ma invia l'hash SHA-512 invece

# Come importare il progetto
E' molto semplice: è necessario git e l'ultima versione di netbeans. Dopo aver clonato la repo, aprire con netbeans i progetti che contiene (LionChat-Server, LionChat-Client e LionChat-Libs). E' possibile successivamente utilizzare "clean and build" di netbeans per compilare i file .jar pronti per la distribuzione che si trovano in /dist all'interno di ogni progetto. E' sufficiente distribuire il client, mentre il server va usato per permettere agli utenti di connettersi.

NB: Per funzionare è necessario impostare il progetto "LionChat_Libs" come dipendenza degli altri due.
Per farlo: tasto destro su uno dei due progetti, proprietà > libraries > rimuovere il progetto e poi riaggiungerlo (add project) per risolvere il problema di dipendenza errata

# Licence
Copyright (C) 2013  Enrico Fasoli

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
