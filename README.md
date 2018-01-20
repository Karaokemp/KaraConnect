Kara Connect!

How to run:
first download and install VLC 64bit from here : https://www.videolan.org/
download and install Java Development Kit 64bit: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
downoad and install maven: https://maven.apache.org/download.cgi

then go to root directory of the repo and run: mvn clean install

a target/Dj Playlist dir will be created 
inside there is the Lanunch DjPlaylist file (choose the one for your OS), make it executable if needed, then run it!

access the web interface of songs requests through: http://localhost:4567/KaraConnect

How to change the web client code:
first work on the client repo: https://github.com/Karaokemp/Karaokemp-client
compile it to production then put the new compiled code into the html directory of the Dj Playlist

How to work on souce code:
you need to install Lombok plugin to you IDE. read about it here: https://projectlombok.org/
then import the project as existing maven project
To launch the project from within the ide create a java application running configuration that VlcjPlayer is the main type
