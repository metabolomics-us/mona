package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.png

import java.awt.{BasicStroke, Color, Font}
import java.io.{ByteArrayOutputStream, File, FileOutputStream, Writer}
import java.util.Base64

import de.erichseifert.gral.data.DataTable
import de.erichseifert.gral.graphics.{Insets2D, Label}
import de.erichseifert.gral.io.plots.DrawableWriterFactory
import de.erichseifert.gral.plots.BarPlot.BarRenderer
import de.erichseifert.gral.plots.XYPlot.XYPlotArea2D
import de.erichseifert.gral.plots.lines.{DefaultLineRenderer2D, LineRenderer}
import de.erichseifert.gral.plots.{BarPlot, Plot, XYPlot}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainWriter

/**
  * Created by sajjan on 4/30/2018.
  */
class PNGWriter extends DomainWriter {

  override val CRLF: Boolean = false


  private def createPlot(spectrum: Spectrum): Plot = {
    // Normalize spectrum
    val ions = spectrum.spectrum.split(' ').map(x => (x.split(':')(0).toDouble, x.split(':')(1).toDouble))
    val maxIntensity: Double = ions.map(_._2).max

    // Build data table
    val data: DataTable = new DataTable(Array(classOf[java.lang.Double], classOf[java.lang.Double]): _*)
    ions.foreach(x => data.add(x._1, 100 * x._2 / maxIntensity))

    // Generate plot
    val plot: BarPlot = new BarPlot(data)
    plot.setBarWidth(0.075)
    plot.setInsets(new Insets2D.Double(10.0, 32.5, 32.5, 5.0))

    val lines: BarRenderer = new BarPlot.BarRenderer(plot)
    lines.setBorderStroke(new BasicStroke(0.5f))
    lines.setBorderColor(Color.BLUE)
    plot.setPointRenderers(data, lines)

    // Add title
    plot.getTitle.setText("MoNA "+ spectrum.id)
    plot.getTitle.setFont(new Font(plot.getTitle.getFont.getFamily, Font.PLAIN, 14))
    plot.setBackground(Color.white)

    // Format plot background and bordder
    plot.getPlotArea.setBorderStroke(new BasicStroke(0.0f))
    plot.getPlotArea.asInstanceOf[XYPlotArea2D].setMajorGridX(false)
    plot.getPlotArea.asInstanceOf[XYPlotArea2D].setMajorGridY(false)

    val xLabel = new Label("m/z")
    xLabel.setFont(new Font(xLabel.getFont.getFamily, Font.PLAIN, 10))

    val yLabel = new Label("Intensity")
    yLabel.setRotation(90)
    yLabel.setFont(new Font(yLabel.getFont.getFamily, Font.PLAIN, 10))

    plot.getAxisRenderer(XYPlot.AXIS_X).setLabel(xLabel)
    plot.getAxisRenderer(XYPlot.AXIS_X).setShapeStroke(new BasicStroke(0.75f))
    plot.getAxisRenderer(XYPlot.AXIS_X).setTickStroke(new BasicStroke(0.75f))
    plot.getAxisRenderer(XYPlot.AXIS_X).setMinorTickStroke(new BasicStroke(0.5f))
    plot.getAxisRenderer(XYPlot.AXIS_X).setTickLength(0.5)
    plot.getAxisRenderer(XYPlot.AXIS_X).setMinorTickLength(0.25)
    plot.getAxisRenderer(XYPlot.AXIS_X).setTickLabelDistance(0.25)
    plot.getAxisRenderer(XYPlot.AXIS_X).setTickFont(new Font(plot.getAxisRenderer(XYPlot.AXIS_X).getTickFont.getFamily, Font.PLAIN, 8))

    plot.getAxis(XYPlot.AXIS_Y).setRange(0, 102.5)
    plot.getAxisRenderer(XYPlot.AXIS_Y).setLabel(yLabel)
    plot.getAxisRenderer(XYPlot.AXIS_Y).setTickLabelRotation(90.0)
    plot.getAxisRenderer(XYPlot.AXIS_Y).setShapeStroke(new BasicStroke(0.75f))
    plot.getAxisRenderer(XYPlot.AXIS_Y).setTickSpacing(25)
    plot.getAxisRenderer(XYPlot.AXIS_Y).setTickStroke(new BasicStroke(0.75f))
    plot.getAxisRenderer(XYPlot.AXIS_Y).setTickLength(0.5)
    plot.getAxisRenderer(XYPlot.AXIS_Y).setMinorTicksVisible(false)
    plot.getAxisRenderer(XYPlot.AXIS_Y).setTickLabelDistance(0.25)
    plot.getAxisRenderer(XYPlot.AXIS_Y).setTickFont(new Font(plot.getAxisRenderer(XYPlot.AXIS_Y).getTickFont.getFamily, Font.PLAIN, 8))

    plot
  }


  /**
    * write the output as a mass spectrum plot
    * @param spectrum
    * @return
    */
  def write(spectrum: Spectrum, writer: Writer): Unit = write(spectrum, writer, 400, 300)

  def write(spectrum: Spectrum, writer: Writer, x: Int, y: Int): Unit = {
    val p = getPrintWriter(writer)
    val plot: Plot = createPlot(spectrum)

    val baos: ByteArrayOutputStream = new ByteArrayOutputStream()
    DrawableWriterFactory.getInstance.get("image/png").write(plot, baos, x, y)
    baos.flush()

    val encodedPlot: String = Base64.getEncoder.encodeToString(baos.toByteArray)
    baos.close()

    p.print(spectrum.id)
    p.print(',')
    p.println(encodedPlot)
    p.flush()
  }
}
