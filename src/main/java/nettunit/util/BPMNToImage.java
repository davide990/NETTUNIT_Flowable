package nettunit.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.canvas.mxICanvas;
import com.mxgraph.io.mxCodec;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import nettunit.dto.ProcessInstanceDetail;
import nettunit.dto.TaskDetails;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.w3c.dom.Node;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.List;

public class BPMNToImage {

    public static BufferedImage getBPMNDiagramImage(BpmnModel bpmnModel, ProcessInstanceDetail prInstance) {
        BpmnAutoLayout layout = new BpmnAutoLayout(bpmnModel, prInstance);
        layout.execute();
        mxGraph graph = layout.getGraph();
        //mxCodec encoder = new mxCodec();
        //Node result = encoder.encode(graph.getModel()); //where graph is the object you are using

        mxStylesheet stylesheet = graph.getStylesheet();
        Hashtable<String, Object> style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_OPACITY, 50);
        style.put(mxConstants.STYLE_FONTCOLOR, "#774400");
        stylesheet.putCellStyle("ROUNDED", style);
        graph.setStylesheet(stylesheet);
        return mxCellRenderer.createBufferedImage(graph, null,
                1, Color.WHITE, true, null);
    }

    public static void saveDiagramToPdf(BpmnModel bpmnModel, String fname) {
        BpmnAutoLayout layout = new BpmnAutoLayout(bpmnModel);
        layout.execute();
        mxGraph graph = layout.getGraph();

        mxCodec encoder = new mxCodec();
        Node result = encoder.encode(graph.getModel()); //where graph is the object you are using

        // you can see the diagram by putting the xml code into
        // https://jgraph.github.io/mxgraph/javascript/examples/editors/diagrameditor.html
        //String xml = mxUtils.getXml(result); //now the global variable 'xml' is assigned with the xml value of the graph

        try {
            mxGraphToPdfFile(graph, fname);
        } catch (FileNotFoundException | DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    private static void mxGraphToPdfFile(mxGraph graph, String fname) throws FileNotFoundException, DocumentException {

        mxStylesheet stylesheet = graph.getStylesheet();
        Hashtable<String, Object> style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_OPACITY, 50);
        style.put(mxConstants.STYLE_FONTCOLOR, "#774400");
        stylesheet.putCellStyle("ROUNDED", style);

        graph.setStylesheet(stylesheet);


        mxRectangle bounds = graph.getGraphBounds();
        Document document = new Document(new Rectangle((float) (bounds
                .getWidth()), (float) (bounds.getHeight())));
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fname));
        document.open();
        final PdfContentByte cb = writer.getDirectContent();

        mxGraphics2DCanvas canvas = (mxGraphics2DCanvas) mxCellRenderer
                .drawCells(graph, null, 1, null, new mxCellRenderer.CanvasFactory() {
                    public mxICanvas createCanvas(int width, int height) {
                        Graphics2D g2 = cb.createGraphics(width, height);
                        return new mxGraphics2DCanvas(g2);
                    }
                });

        canvas.getGraphics().dispose();
        document.close();
    }

}
