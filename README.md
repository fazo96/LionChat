# LionChat
Multiplatform server/client socket based text chat written in Java with sophisticated features **already implemented**, such as:
- Realtime text chat
- Works on Linux, Mac OS X and Windows (Only tested on linux and briefly on windows. Try it! Should work!)
- Multilanguage support with easy to write language files, with english being the default
- Secure account system with no password exposure (at least I think so. Have a look at the code!)
- Extensive command system for dynamic server, account, groups and permission management (almost finished but usable)
- Administrator users that can run server commands
- Ability to join channels and password protect them
- Easily configurable, comfortable GUI for the client
- Headless server (no GUI required)

**Working on:**
- Fixing bugs (stable branch)
- Digital Signature and Encryption (developement branch)

**Planned:**
- Client code reorganization (also CLI client)
- Anti spam tweaks
- Settings files rewrite
- Better Server and Client output handling
- Server bandwidth management
- Logging

**Non guaranteed long term goals:**
- HTML formatted text
- GUI commands, more interactive client
- Server command system rewrite
- Voice chat
- peer to peer (will probably be done by rewriting everything)

How to use it
==============
You need the *latest version* of Java installed and *working*.
I will provide links to precompiled jar files in a while. Try the below paragraph for now so you can also edit the code. Or you can provide builds yourself!

**Note**: the client is ideally a program designed to be used by a non technical person. At the current state, you can say it has achieved this pretty well. However, I won't waste time writing an easy interface for the server.
**About language files**: it is important that you have the "en" language file intact, so if you want to experiment, make a backup of the file first. *The client and server language files are separated, but may have sentences in common*.

How to write code for it
==============
Make sure you read the 'how to use it' paragraph. You need the *latest version* of Git, Netbeans and of course the JDK7 (Either oracle or openJDK).
- Clone the repo
- Open the 3 projects (LionChat Client, LionChat Libs, Lionchat Server) using Netbeans by navigating into the repo folder
- For the client and server projects, open the project's proprieties in Netbeans and **set their src folder as the working directory**. Just write "src" in the working directory text field.
- For the client and server projects, **set the other project as a dependency** if it's not already set correctly. (Netbeans will tell you)
- Edit the code or press "Clean and build" to generate redistributable .jar executables using Netbeans.

If you don't know how to do the above or don't understand it, you *clearly* need to learn basic Git and Netbeans functionality before you can tinker with the program. Google may help you.

Faq
----
**But I love eclipse! How do you import in eclipse?**

I'm sorry, I don't like eclipse much. Of course you can code with eclipse, but it may be hard to get the project to work and I don't support it.

**But I don't use IDEs or I don't want to! How do I compile from command line?**

I never tried it, but you should be able to do it if you have experiance with manually compiling. Just read the netbeans part and try. I don't support this though. Netbeans is free and open source for Windows, Mac and Linux, try it.

License
==============
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
