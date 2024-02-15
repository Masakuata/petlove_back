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
	public static float TITLE_FONT_SIZE = 20F;

	public static Document setupNewDocument(PdfWriter writer) {
		PdfDocument pdf = new PdfDocument(writer);
		pdf.setDefaultPageSize(PageSize.LETTER);
		Document document = new Document(pdf);
		document.setFontSize(DEFAULT_FONT_SIZE);
		return document;
	}

	public static Document setupNewTicket(PdfWriter writer, float height) {
		DEFAULT_FONT_SIZE = 8F;
		TITLE_FONT_SIZE = 8F;
		LOGO_SQR_SIZE = 10F;
		LOGO_H_OFFSET = 6F;
		LOGO_V_OFFSET = 8F;
		PdfDocument pdf = new PdfDocument(writer);
		return new Document(pdf, new PageSize(new Rectangle(360F, height)));
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
		return new Cell().add(new Paragraph(text)).setBorder(Border.NO_BORDER);
	}

	public static Cell getTopBorderCell(String text) {
		return new Cell().add(new Paragraph(text)).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1F));
	}

	public static Cell getBottomBorderCell(String text) {
		return new Cell().add(new Paragraph(text)).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1F));
	}
}
