# Distributing OutputStream

## The problem
Whereas Dart has `StreamController`s, Java has no good facilities to distribute data among multiple _stream sinks_ without writing copious amounts of boilerplate code. 

## The solution
Having moved the boilerplate code to a separate JAR, I've decided to release it and spare others from having to implement everything from scratch.

The Distributor takes an arbitrary number of `OutputStream`s, storing them in an internal map. Each stream can be set to survive the distributor or be closed along with it. Once the distributor is finalized (set to writeable), any writes
 are forwarded to all streams stored in the distributor, but new streams may not be added.
Once all data is written out, calling `close()` on the distributor will close all streams that were marked as closeable, while those marked as surviving will be left open to allow further access to the contained data. These streams need 
to be closed separately.

## Example
```java
		FileOutputStream mockFos = new FileOutputStream("test.file");
		ByteArrayOutputStream mockBaos = new ByteArrayOutputStream();

		DistributingOutputStream dst = new DistributingOutputStream();
		dst.addReceiver(mockBaos, true);
		dst.addReceiver(mockFos, true);
		dst.setWriteable(true);

		dst.write(128);
```