package edu.ucdavis.fiehnlab.mona.backend.core.domain.io

import java.io.{IOException, InterruptedIOException, PrintWriter, Writer}


class CRLFPrintWriter(out: Writer) extends PrintWriter(out) {

  override def println(): Unit = {
    try {
      if (out == null) {
        throw new IOException("Stream closed")
      }

      out.write("\r\n")
    } catch {
      case x: InterruptedIOException =>
        Thread.currentThread.interrupt()
      case x: IOException =>
        setError()
    }
  }
}
