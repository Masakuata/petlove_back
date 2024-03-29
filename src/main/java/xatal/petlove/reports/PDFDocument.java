package xatal.petlove.reports;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;

import java.net.MalformedURLException;

public abstract class PDFDocument {
	public static final String LOGO_PATH = "src/main/resources/pet-icon.png";
	public static float LOGO_SQR_SIZE = 50F;
	public static float LOGO_H_OFFSET = 30F;
	public static float LOGO_V_OFFSET = 40F;
	public static float DEFAULT_FONT_SIZE = 10F;
	public static float TITLE_FONT_SIZE = 17F;

	public static Document setupNewDocument(PdfWriter writer) {
		PDFDocument.setDocumentSizes();
		PdfDocument pdf = new PdfDocument(writer);
		pdf.setDefaultPageSize(PageSize.LETTER);
		Document document = new Document(pdf);
		document.setFontSize(DEFAULT_FONT_SIZE);
		return document;
	}

	public static Document setupNewTicket(PdfWriter writer, float height) {
		PDFDocument.setTicketSizes();
		PdfDocument pdf = new PdfDocument(writer);
		return new Document(pdf, new PageSize(new Rectangle(480F, height)));
	}

	private static void setDocumentSizes() {
		DEFAULT_FONT_SIZE = 10F;
		TITLE_FONT_SIZE = 20F;
		LOGO_SQR_SIZE = 50F;
		LOGO_H_OFFSET = 30F;
		LOGO_V_OFFSET = 40F;
	}

	private static void setTicketSizes() {
		DEFAULT_FONT_SIZE = 30F;
		TITLE_FONT_SIZE = 30F;
		LOGO_SQR_SIZE = 10F;
		LOGO_H_OFFSET = 6F;
		LOGO_V_OFFSET = 8F;
	}

	public static Document setupNewTicket(PdfWriter writer) {
		return setupNewTicket(writer, 1440F);
	}

	public static Paragraph getAsTitle(String text) {
		return new Paragraph(text)
			.setFontSize(TITLE_FONT_SIZE)
			.setKeepTogether(true);
	}

	public static Paragraph emptyNewLine() {
		return new Paragraph()
			.setFontSize(DEFAULT_FONT_SIZE)
			.setKeepTogether(true);
	}

	public static Image getLogo() throws MalformedURLException {
		Image image = new Image(ImageDataFactory.create(LOGO_PATH));
		image.setHeight(LOGO_SQR_SIZE);
		image.setWidth(LOGO_SQR_SIZE);
		image.setFixedPosition(PageSize.LETTER.getRight() - LOGO_SQR_SIZE - LOGO_H_OFFSET,
			PageSize.LETTER.getTop() - LOGO_SQR_SIZE - LOGO_V_OFFSET);
		return image;
	}

	public static Cell getNoBorderCell(String text) {
		return getNoBorderCell(text, 1, 1);
	}

	public static Cell getNoBorderCell(String text, int rowspan, int colspan) {
		return new Cell(rowspan, colspan).add(new Paragraph(text)).setBorder(Border.NO_BORDER);
	}

	public static Cell getTopBorderCell(String text) {
		return new Cell().add(new Paragraph(text)).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1F));
	}

	public static Cell getBottomBorderCell(String text) {
		return new Cell().add(new Paragraph(text)).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1F));
	}
}
