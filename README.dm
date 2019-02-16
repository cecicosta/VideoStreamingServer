### What is in this repository? ###

A server application to receive and play video stream via sockets in a local network. When running the server waits for a connection from a cliente application.
The protocol to receive the streaming is implemented as following:

After stablishing a connection, the server waits for the first message, which must be the resolution of the video stream in the following format:

´´´
width,height
´´´

The application the waits to receive, as string, the amount of byte to read for the next frame.

´´´
totalBytesToRead
´´´

The server sends the message:

´´´
REQUEST_FILE:totalBytesToRead
´´´

To confirm it receive the amount of bytes to read correctly and notifies it is ready to read the frame.
#### It must be in raw RGB 24bits format. #### 

After receiving the exact amount of bytes expected, it return the message:

´´´
ALL_BYTES_RECEIVED
´´´

In case of a mismatch in the amount of bytes expected and received, the message:

´´´
BYTES_MISMATCH
´´´

is sent.

