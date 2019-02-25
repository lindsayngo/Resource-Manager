Lindsay Ngo
Operating Systems Lab 3: Resource Management
The goal of this lab is to do resource allocation using both an optimistic resource manager and the banker’s
algorithm of Dijkstra. The optimistic resource manager is: Satisfy a request if possible, if not make the
task wait; when a release occurs, try to satisfy pending requests in a FIFO manner.

To run this program:

javac Lab3.java
java Lab3 *input*

