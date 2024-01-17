package xatal.petlove;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xatal.petlove.reports.ReportableField;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class SharedzApplication {

	public static void main(String[] args) {
		SpringApplication.run(SharedzApplication.class, args);
//		String pdfPath = "file.pdf";
//		try (PdfWriter writer = new PdfWriter(pdfPath)) {
//			Document document = new Document(new PdfDocument(writer), PageSize.LETTER);
//			Table table = new Table(2);
//			table.setWidth(UnitValue.createPercentValue(100));
//			Cell headCliente = new Cell();
//			headCliente.add(new Paragraph("Cliente"));
//
//			Cell headTotal = new Cell();
//			headTotal.add(new Paragraph("Total"));
//
//			Cell valueCliente = new Cell();
//			valueCliente.add(new Paragraph("Edson"));
//
//			Cell valueTotal = new Cell();
//			valueTotal.add(new Paragraph("$200"));
//
//			table.addHeaderCell(headCliente);
//			table.addHeaderCell(headTotal);
//			table.addCell(valueCliente);
//			table.addCell(valueTotal);
//			document.add(table);
//			document.close();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
	}

	private static void printValor(Object reportable) {
		List<Field> fields = Arrays.stream(reportable.getClass().getDeclaredFields()).toList();
		fields.forEach(field -> {
			field.setAccessible(true);
			if (!field.getAnnotation(ReportableField.class).getValueFrom().isEmpty()) {
				String method = field.getAnnotation(ReportableField.class).getValueFrom();
				try {
					String value = (String) reportable.getClass().getMethod(method).invoke(reportable);
					System.out.println(value);
				} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			} else {
				try {
					System.out.println(field.get(reportable));
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

}

class Prueba {
	@ReportableField(headerName = "Prueba")
	private String valor = "Prueba";

	@ReportableField(headerName = "Otra prueba", getValueFrom = "getValor")
	private Prueba prueba;

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public Prueba getPrueba() {
		return prueba;
	}

	public void setPrueba(Prueba prueba) {
		this.prueba = prueba;
	}
}