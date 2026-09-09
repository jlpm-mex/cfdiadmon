package net.ellapiz.admoncfdiprov.util;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import net.ellapiz.admoncfdiprov.vo.CfdiRecibidoVO;
import net.ellapiz.admoncfdiprov.vo.ComprobanteVO;
import net.ellapiz.admoncfdiprov.vo.DoctoRelacionadoVO;
import net.ellapiz.admoncfdiprov.vo.EmisorVO;
import net.ellapiz.admoncfdiprov.vo.ImpuestosVO;
import net.ellapiz.admoncfdiprov.vo.ItemVO;
import net.ellapiz.admoncfdiprov.vo.PagoItemVO;
import net.ellapiz.admoncfdiprov.vo.PagoVO;
import net.ellapiz.admoncfdiprov.vo.ReceptorVO;
import net.ellapiz.admoncfdiprov.vo.TimbreVO;
import net.ellapiz.admoncfdiprov.vo.TrasladoVO;

@Component
public class CfdiPdfGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final float MARGIN = 50;
    private static final float MARGIN_BOTTOM = 50;
    private static final float LINE_HEIGHT = 16;
    private static final float TITLE_HEIGHT = 25;
    private static final float SECTION_SEPARATOR = 10;
    
    // Hacer estático para que todas las instancias compartan la misma definición
    private static final PDRectangle LANDSCAPE_LETTER = new PDRectangle(
        PDRectangle.LETTER.getHeight(), 
        PDRectangle.LETTER.getWidth()
    );

    // ==================== CLASE INTERNA PDF CONTEXT ====================
    
    private static class PdfContext {
        private final PDDocument document;
        private PDPage currentPage;
        private PDPageContentStream cs;
        private float y;
        private final PDFont bold;
        private final PDFont regular;
        private final PDFont small;
        private final float pageHeight;
        private final float pageWidth;

        public PdfContext(PDDocument document, PDPage page, PDFont bold, PDFont regular, PDFont small) 
                throws IOException {
            this.document = document;
            this.currentPage = page;
            this.cs = new PDPageContentStream(document, page);
            this.y = page.getMediaBox().getHeight() - MARGIN;
            this.bold = bold;
            this.regular = regular;
            this.small = small;
            this.pageHeight = page.getMediaBox().getHeight();
            this.pageWidth = page.getMediaBox().getWidth();
        }

        private PdfContext(PDDocument document, PDPage page, PDPageContentStream cs, float y, 
                           PDFont bold, PDFont regular, PDFont small, float pageHeight, float pageWidth) {
            this.document = document;
            this.currentPage = page;
            this.cs = cs;
            this.y = y;
            this.bold = bold;
            this.regular = regular;
            this.small = small;
            this.pageHeight = pageHeight;
            this.pageWidth = pageWidth;
        }

        public PdfContext ensureSpace(float neededSpace) throws IOException {
            if (y - neededSpace < MARGIN_BOTTOM) {
                // Cerrar stream actual
                cs.close();
                
                // Crear nueva página horizontal USANDO LANDSCAPE_LETTER DIRECTAMENTE
                PDPage newPage = new PDPage(LANDSCAPE_LETTER);
                document.addPage(newPage);
                
                this.currentPage = newPage;
                this.cs = new PDPageContentStream(document, newPage);
                this.y = pageHeight - MARGIN;
            }
            return this;
        }

        public void close() throws IOException {
            if (cs != null) {
                cs.close();
            }
        }

        // Getters
        public PDDocument getDocument() { return document; }
        public PDPage getCurrentPage() { return currentPage; }
        public PDPageContentStream getCs() { return cs; }
        public float getY() { return y; }
        public PDFont getBold() { return bold; }
        public PDFont getRegular() { return regular; }
        public PDFont getSmall() { return small; }
        public float getPageHeight() { return pageHeight; }
        public float getPageWidth() { return pageWidth; }
    }

    // ==================== MÉTODO PRINCIPAL ====================

    public byte[] generatePdf(ComprobanteVO comprobanteVO) throws IOException {
        try (PDDocument document = new PDDocument()) {
            // Crear primera página horizontal
            PDPage page = new PDPage(LANDSCAPE_LETTER);
            document.addPage(page);

            // Cargar fuentes
            PDFont fontRegular = loadFont(document, "fonts/arial.ttf");
            PDFont fontBold = loadFont(document, "fonts/arialbd.ttf");
            PDFont fontSmall = loadFont(document, "fonts/arial.ttf");

            // Inicializar contexto
            PdfContext ctx = new PdfContext(document, page, fontBold, fontRegular, fontSmall);

            // === DIBUJAR SECCIONES ===
            ctx = drawHeader(ctx, comprobanteVO);
            ctx = drawComprobanteInfo(ctx, comprobanteVO);
            ctx = drawEmisor(ctx, comprobanteVO.getEmisorVO());
            ctx = drawReceptor(ctx, comprobanteVO.getReceptorVO());

            // Conceptos (solo si no es complemento de pago)
            if (!"P".equals(comprobanteVO.getTipoDeComprobante())) {
                ctx = drawConceptos(ctx, ((CfdiRecibidoVO) comprobanteVO).getItems());
            }

            ctx = drawImpuestos(ctx, comprobanteVO.getImpuestosVO());
            ctx = drawTotal(ctx, comprobanteVO);
            ctx = drawTimbre(ctx, comprobanteVO.getTimbreVO());

            // Complemento de pago (si aplica)
            if ("P".equals(comprobanteVO.getTipoDeComprobante())) {
                ctx = drawPagoComplemento(ctx, (PagoVO) comprobanteVO);
            }

            // Cerrar el stream final
            ctx.close();

            // Guardar documento
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    // ==================== MÉTODOS DE DIBUJO ====================

    private PdfContext drawHeader(PdfContext ctx, ComprobanteVO comprobanteVO) throws IOException {
        float needed = TITLE_HEIGHT + SECTION_SEPARATOR;
        ctx = ctx.ensureSpace(needed);

        PDPageContentStream cs = ctx.getCs();
        float y = ctx.getY();
        PDFont bold = ctx.getBold();
        float pageWidth = ctx.getPageWidth();

        // Título del documento
        String title = "CFDI - " + ("P".equals(comprobanteVO.getTipoDeComprobante()) ? 
            "COMPLEMENTO DE PAGO" : "COMPROBANTE FISCAL");
        
        cs.setFont(bold, 14);
        cs.beginText();
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(title);
        cs.endText();
        y -= TITLE_HEIGHT;

        // Línea separadora
        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(1);
        cs.moveTo(MARGIN, y);
        cs.lineTo(pageWidth - MARGIN, y);
        cs.stroke();
        y -= SECTION_SEPARATOR;

        return new PdfContext(ctx.getDocument(), ctx.getCurrentPage(), ctx.getCs(), y,
                              ctx.getBold(), ctx.getRegular(), ctx.getSmall(),
                              ctx.getPageHeight(), ctx.getPageWidth());
    }

    private PdfContext drawComprobanteInfo(PdfContext ctx, ComprobanteVO comprobanteVO) throws IOException {
        float needed = LINE_HEIGHT + LINE_HEIGHT + 5;
        ctx = ctx.ensureSpace(needed);

        PDPageContentStream cs = ctx.getCs();
        float y = ctx.getY();
        PDFont bold = ctx.getBold();
        PDFont regular = ctx.getRegular();

        cs.setFont(bold, 10);
        cs.beginText();
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("DATOS DEL COMPROBANTE:");
        cs.endText();
        y -= LINE_HEIGHT;

        cs.setFont(regular, 9);
        String tipo = comprobanteVO.getTipoDeComprobante() != null ?
            ("I".equals(comprobanteVO.getTipoDeComprobante()) ? "INGRESO" :
             "P".equals(comprobanteVO.getTipoDeComprobante()) ? "PAGO" : 
             comprobanteVO.getTipoDeComprobante()) : "";
        
        String info = String.format("Serie: %s  |  Folio: %s  |  Fecha: %s  |  Moneda: %s  |  Tipo: %s",
            comprobanteVO.getFcSerie() != null ? comprobanteVO.getFcSerie() : "",
            comprobanteVO.getFcFolio() != null ? comprobanteVO.getFcFolio() : "",
            comprobanteVO.getFdFechaComprobante() != null ? comprobanteVO.getFdFechaComprobante() : "",
            comprobanteVO.getFcMoneda() != null ? comprobanteVO.getFcMoneda() : "XXX",
            tipo
        );
        
        cs.beginText();
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(info);
        cs.endText();
        y -= LINE_HEIGHT + 5;

        return new PdfContext(ctx.getDocument(), ctx.getCurrentPage(), ctx.getCs(), y,
                              ctx.getBold(), ctx.getRegular(), ctx.getSmall(),
                              ctx.getPageHeight(), ctx.getPageWidth());
    }

    private PdfContext drawEmisor(PdfContext ctx, EmisorVO emisor) throws IOException {
        float needed = LINE_HEIGHT + LINE_HEIGHT + 5;
        ctx = ctx.ensureSpace(needed);

        PDPageContentStream cs = ctx.getCs();
        float y = ctx.getY();
        PDFont bold = ctx.getBold();
        PDFont regular = ctx.getRegular();

        cs.setFont(bold, 10);
        cs.beginText();
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("EMISOR:");
        cs.endText();
        y -= LINE_HEIGHT;

        cs.setFont(regular, 9);
        String info = String.format("RFC: %s  |  Nombre: %s  |  Régimen: %s",
            emisor.getFcRfc() != null ? emisor.getFcRfc() : "",
            emisor.getFcnombre() != null ? emisor.getFcnombre() : "",
            emisor.getFcRegimenFiscal() != null ? emisor.getFcRegimenFiscal() : ""
        );
        cs.beginText();
        cs.newLineAtOffset(MARGIN + 10, y);
        cs.showText(info);
        cs.endText();
        y -= LINE_HEIGHT + 5;

        return new PdfContext(ctx.getDocument(), ctx.getCurrentPage(), ctx.getCs(), y,
                              ctx.getBold(), ctx.getRegular(), ctx.getSmall(),
                              ctx.getPageHeight(), ctx.getPageWidth());
    }

    private PdfContext drawReceptor(PdfContext ctx, ReceptorVO receptorVO) throws IOException {
        float needed = LINE_HEIGHT + LINE_HEIGHT + 5;
        ctx = ctx.ensureSpace(needed);
        
        PDPageContentStream cs = ctx.getCs();
        float y = ctx.getY();
        PDFont bold = ctx.getBold();
        PDFont regular = ctx.getRegular();
        
        cs.setFont(bold, 10);
        cs.beginText();
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("RECEPTOR:");
        cs.endText();
        y -= LINE_HEIGHT;

        cs.setFont(regular, 9);
        String info = String.format("RFC: %s  |  Nombre: %s  |  Domicilio: %s  |  Uso CFDI: %s",
            receptorVO.getFcRfc() != null ? receptorVO.getFcRfc() : "",
            receptorVO.getFcNombre() != null ? receptorVO.getFcNombre() : "",
            receptorVO.getFcDomicilioFiscalReceptor() != null ? receptorVO.getFcDomicilioFiscalReceptor() : "",
            receptorVO.getFcUsoCFDI() != null ? receptorVO.getFcUsoCFDI() : ""
        );
        cs.beginText();
        cs.newLineAtOffset(MARGIN + 10, y);
        cs.showText(info);
        cs.endText();
        y -= LINE_HEIGHT + 5;

        return new PdfContext(ctx.getDocument(), ctx.getCurrentPage(), ctx.getCs(), y,
                              ctx.getBold(), ctx.getRegular(), ctx.getSmall(),
                              ctx.getPageHeight(), ctx.getPageWidth());
    }

    private PdfContext drawConceptos(PdfContext ctx, List<ItemVO> items) throws IOException {
        if (items == null || items.isEmpty()) {
            return ctx;
        }

        PDPageContentStream cs = ctx.getCs();
        float y = ctx.getY();
        PDFont bold = ctx.getBold();
        PDFont regular = ctx.getRegular();
        float pageWidth = ctx.getPageWidth();

        // === TÍTULO Y ENCABEZADOS ===
        float headerSpace = LINE_HEIGHT + LINE_HEIGHT + 10;
        ctx = ctx.ensureSpace(headerSpace);
        y = ctx.getY();
        cs = ctx.getCs();

        // Título
        cs.setFont(bold, 10);
        cs.beginText();
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("CONCEPTOS:");
        cs.endText();
        y -= LINE_HEIGHT;

        // Encabezados de tabla
        float[] colWidths = {70, 70, 250, 100, 100, 100};
        float x = MARGIN;
        String[] headers = {"Cant.", "Clave", "Descripción", "V. Unitario", "Importe", "Descuento"};

        cs.setFont(bold, 8);
        for (int i = 0; i < headers.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(headers[i]);
            cs.endText();
            x += colWidths[i];
        }
        y -= LINE_HEIGHT;

        // Línea separadora
        cs.setStrokingColor(Color.LIGHT_GRAY);
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, y);
        cs.lineTo(pageWidth - MARGIN, y);
        cs.stroke();
        y -= 10;

        // Actualizar contexto después del header
        ctx = new PdfContext(ctx.getDocument(), ctx.getCurrentPage(), cs, y,
                             ctx.getBold(), ctx.getRegular(), ctx.getSmall(),
                             ctx.getPageHeight(), ctx.getPageWidth());

        // === DIBUJAR CADA CONCEPTO ===
        for (ItemVO itemVO : items) {
            // Verificar espacio para UNA fila de concepto
            ctx = ctx.ensureSpace(LINE_HEIGHT + 5);
            y = ctx.getY();
            cs = ctx.getCs();

            //️ RESTABLECER LA FUENTE DESPUÉS DE ensureSpace
            cs.setFont(regular, 8);

            x = MARGIN;
            
            // Cantidad
            DecimalFormat df = new DecimalFormat("0.##");
            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(df.format(itemVO.getFdCantidad()));
            cs.endText();
            x += colWidths[0];

            // Clave/NoIdentificacion
            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(itemVO.getFcNumeroDeIdentificacion() != null ? 
                itemVO.getFcNumeroDeIdentificacion() : "");
            cs.endText();
            x += colWidths[1];

            // Descripción (truncada para no encimarse)
            String desc = itemVO.getFcDescripcion();
            if (desc.length() > 63) desc = desc.substring(0, 63) + "...";
            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(desc);
            cs.endText();
            x += colWidths[2];

            // Valor Unitario
            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(String.format("$%,.2f", itemVO.getFdPrecioUnitario()));
            cs.endText();
            x += colWidths[3];

            // Importe
            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(String.format("$%,.2f", itemVO.getFdImporte()));
            cs.endText();
            x += colWidths[4];

            // Descuento
            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(itemVO.getFdDescuento().compareTo(BigDecimal.ZERO) > 0 ? 
                String.format("-$%,.2f", itemVO.getFdDescuento()) : "-");
            cs.endText();

            y -= LINE_HEIGHT;
            
            // Actualizar contexto después de cada concepto
            ctx = new PdfContext(ctx.getDocument(), ctx.getCurrentPage(), cs, y,
                                 ctx.getBold(), ctx.getRegular(), ctx.getSmall(),
                                 ctx.getPageHeight(), ctx.getPageWidth());
        }

        // Espacio final después de los conceptos
        y -= 5;
        return new PdfContext(ctx.getDocument(), ctx.getCurrentPage(), ctx.getCs(), y,
                              ctx.getBold(), ctx.getRegular(), ctx.getSmall(),
                              ctx.getPageHeight(), ctx.getPageWidth());
    }

    private PdfContext drawImpuestos(PdfContext ctx, ImpuestosVO impuestosVO) throws IOException {
        if (impuestosVO == null || impuestosVO.getTraslados() == null || impuestosVO.getTraslados().isEmpty()) {
            return ctx;
        }

        float needed = LINE_HEIGHT + (impuestosVO.getTraslados().size() * LINE_HEIGHT) + 
                       (impuestosVO.getTotalImpuestosTrasladados() > 0 ? LINE_HEIGHT : 0) + 10;
        ctx = ctx.ensureSpace(needed);

        PDPageContentStream cs = ctx.getCs();
        float y = ctx.getY();
        PDFont bold = ctx.getBold();
        PDFont regular = ctx.getRegular();

        cs.setFont(bold, 10);
        cs.beginText();
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("IMPUESTOS:");
        cs.endText();
        y -= LINE_HEIGHT;

        cs.setFont(regular, 9);
        for (TrasladoVO trasladoVO : impuestosVO.getTraslados()) {
            String info = String.format("  %s: Base $%,.2f  |  Tasa %,.2f%%  |  Importe $%,.2f",
                "002".equals(trasladoVO.getImpuesto()) ? "IVA" : trasladoVO.getImpuesto(),
                trasladoVO.getBase(),
                trasladoVO.getTasaOCuota().multiply(new BigDecimal("100.00")),
                trasladoVO.getImporte()
            );
            cs.beginText();
            cs.newLineAtOffset(MARGIN + 10, y);
            cs.showText(info);
            cs.endText();
            y -= LINE_HEIGHT;
        }

        if (impuestosVO.getTotalImpuestosTrasladados() > 0) {
            cs.setFont(bold, 9);
            cs.beginText();
            cs.newLineAtOffset(MARGIN + 10, y);
            cs.showText(String.format("Total trasladados: $%,.2f", impuestosVO.getTotalImpuestosTrasladados()));
            cs.endText();
            y -= LINE_HEIGHT + 5;
        }

        return new PdfContext(ctx.getDocument(), ctx.getCurrentPage(), ctx.getCs(), y,
                              ctx.getBold(), ctx.getRegular(), ctx.getSmall(),
                              ctx.getPageHeight(), ctx.getPageWidth());
    }

    private PdfContext drawTotal(PdfContext ctx, ComprobanteVO comprobanteVO) throws IOException {
        float needed = 15 + 25 + LINE_HEIGHT + 5;
        ctx = ctx.ensureSpace(needed);

        PDPageContentStream cs = ctx.getCs();
        float y = ctx.getY();
        PDFont bold = ctx.getBold();
        PDFont regular = ctx.getRegular();
        float pageWidth = ctx.getPageWidth();

        // Línea separadora
        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(1);
        cs.moveTo(MARGIN, y);
        cs.lineTo(pageWidth - MARGIN, y);
        cs.stroke();
        y -= 15;

        // Total
        cs.setFont(bold, 12);
        cs.beginText();
        cs.newLineAtOffset(pageWidth - MARGIN - 200, y);
        cs.showText("TOTAL:  $");
        cs.endText();

        cs.setFont(bold, 14);
        cs.beginText();
        cs.newLineAtOffset(pageWidth - MARGIN - 100, y);
        cs.showText(String.format("%,.2f", comprobanteVO.getFdTotal()));
        cs.endText();
        y -= 25;

        // Subtotal y descuento
        cs.setFont(regular, 9);
        String subtotalInfo = String.format("Subtotal: $%,.2f", comprobanteVO.getSubTotal());
        if (comprobanteVO.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
            subtotalInfo += String.format("  |  Descuento: -$%,.2f", comprobanteVO.getDescuento());
        }
        cs.beginText();
        cs.newLineAtOffset(pageWidth - MARGIN - 250, y);
        cs.showText(subtotalInfo);
        cs.endText();
        y -= LINE_HEIGHT + 5;

        return new PdfContext(ctx.getDocument(), ctx.getCurrentPage(), ctx.getCs(), y,
                              ctx.getBold(), ctx.getRegular(), ctx.getSmall(),
                              ctx.getPageHeight(), ctx.getPageWidth());
    }

    private PdfContext drawTimbre(PdfContext ctx, TimbreVO timbreVO) throws IOException {
        if (timbreVO == null) {
            return ctx;
        }

        float needed = LINE_HEIGHT + LINE_HEIGHT + 5;
        ctx = ctx.ensureSpace(needed);

        PDPageContentStream cs = ctx.getCs();
        float y = ctx.getY();
        PDFont bold = ctx.getBold();
        PDFont regular = ctx.getRegular();

        cs.setFont(bold, 10);
        cs.beginText();
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("TIMBRE FISCAL:");
        cs.endText();
        y -= LINE_HEIGHT;

        cs.setFont(regular, 8);
        String info = String.format("UUID: %s  |  Fecha Timbrado: %s  |  RFC Prov. Certif.: %s",
            timbreVO.getUuid() != null ? timbreVO.getUuid() : "",
            timbreVO.getFechaTimbrado() != null ? timbreVO.getFechaTimbrado().format(DATE_FORMATTER) : "",
            timbreVO.getRfcProvCertif() != null ? timbreVO.getRfcProvCertif() : ""
        );
        cs.beginText();
        cs.newLineAtOffset(MARGIN + 10, y);
        cs.showText(info);
        cs.endText();
        y -= LINE_HEIGHT;

        return new PdfContext(ctx.getDocument(), ctx.getCurrentPage(), ctx.getCs(), y,
                              ctx.getBold(), ctx.getRegular(), ctx.getSmall(),
                              ctx.getPageHeight(), ctx.getPageWidth());
    }

    private PdfContext drawPagoComplemento(PdfContext ctx, PagoVO pagoVO) throws IOException {
        if (pagoVO == null) {
            return ctx;
        }

        // Espacio estimado para el complemento de pago
        float needed = LINE_HEIGHT + LINE_HEIGHT + 10;
        if (pagoVO.getPagos() != null) {
            needed += pagoVO.getPagos().size() * LINE_HEIGHT * 2;
        }
        ctx = ctx.ensureSpace(needed);

        PDPageContentStream cs = ctx.getCs();
        float y = ctx.getY();
        PDFont bold = ctx.getBold();
        PDFont regular = ctx.getRegular();
        float pageWidth = ctx.getPageWidth();

        // Línea separadora antes del complemento
        y -= 10;
        cs.setStrokingColor(Color.black);
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, y);
        cs.lineTo(pageWidth - MARGIN, y);
        cs.stroke();
        y -= 15;

        // Título del complemento
        cs.setFont(bold, 12);
        cs.beginText();
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("COMPLEMENTO DE PAGO");
        cs.endText();
        y -= LINE_HEIGHT + 10;

        // === MONTO TOTAL DE PAGOS (destacado) ===
        cs.setFont(bold, 11);
        String montoTotalStr = pagoVO.getMontoTotalPagos() != null ? 
            String.format("$%,.2f", pagoVO.getMontoTotalPagos()) : "$0.00";
        String totalPagosLabel = "Monto total de pagos:";
        
        // Alinear a la derecha
        float labelWidth = bold.getStringWidth(totalPagosLabel) / 1000 * 11;
        float montoWidth = bold.getStringWidth(montoTotalStr) / 1000 * 11;
        float totalWidth = labelWidth + montoWidth + 10;
        
        cs.setFont(bold, 11);
        cs.beginText();
        cs.newLineAtOffset(pageWidth - MARGIN - totalWidth, y);
        cs.showText(totalPagosLabel + " ");
        cs.endText();
        
        cs.setFont(bold, 14);
        cs.setNonStrokingColor(Color.BLACK);
        cs.beginText();
        cs.newLineAtOffset(pageWidth - MARGIN - montoWidth, y);
        cs.showText(montoTotalStr);
        cs.endText();
        y -= LINE_HEIGHT + 10;

        // Línea separadora
        cs.setStrokingColor(Color.LIGHT_GRAY);
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, y);
        cs.lineTo(pageWidth - MARGIN, y);
        cs.stroke();
        y -= 10;

        // === DETALLE DE CADA PAGO ===
        if (pagoVO.getPagos() != null && !pagoVO.getPagos().isEmpty()) {
            cs.setFont(bold, 10);
            cs.beginText();
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("Detalle de pagos:");
            cs.endText();
            y -= LINE_HEIGHT;

            // Encabezados de tabla
            float[] colWidths = {120, 160, 80, 200};
            float x = MARGIN;
            String[] headers = {"Fecha", "Forma de Pago", "Monto", "Documento"};

            cs.setFont(bold, 8);
            for (int i = 0; i < headers.length; i++) {
                cs.beginText();
                cs.newLineAtOffset(x, y);
                cs.showText(headers[i]);
                cs.endText();
                x += colWidths[i];
            }
            y -= LINE_HEIGHT;

            // Línea separadora
            cs.setStrokingColor(Color.LIGHT_GRAY);
            cs.setLineWidth(0.5f);
            cs.moveTo(MARGIN, y);
            cs.lineTo(pageWidth - MARGIN, y);
            cs.stroke();
            y -= 10;

            // Dibujar cada pago
            cs.setFont(regular, 8);
            for (PagoItemVO pagoItem : pagoVO.getPagos()) {
                ctx = ctx.ensureSpace(LINE_HEIGHT + 5);
                cs = ctx.getCs();
                
                // Restablecer fuente después de ensureSpace
                cs.setFont(regular, 8);

                x = MARGIN;
                
                // Fecha
                String fecha = pagoItem.getFdFechaPago() != null ? 
                    pagoItem.getFdFechaPago().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
                cs.beginText();
                cs.newLineAtOffset(x, y);
                cs.showText(fecha);
                cs.endText();
                x += colWidths[0];

                // Forma de pago
                String forma = pagoItem.getFcFormaDePagoP() != null ? 
                    getFormaPagoDescripcion(pagoItem.getFcFormaDePagoP()) : "";
                cs.beginText();
                cs.newLineAtOffset(x, y);
                cs.showText(forma);
                cs.endText();
                x += colWidths[1];

                // Monto
                String montoStr = pagoItem.getFdMonto() != null ? 
                    String.format("$%,.2f", pagoItem.getFdMonto()) : "$0.00";
                cs.beginText();
                cs.newLineAtOffset(x, y);
                cs.showText(montoStr);
                cs.endText();
                x += colWidths[2];

                String documento = "";
                if (pagoItem.getDoctosRelacionados() != null && !pagoItem.getDoctosRelacionados().isEmpty()) {
                	documento = pagoItem.getDoctosRelacionados().stream()
                	.map(DoctoRelacionadoVO::getIdDocumento)
                	.collect(Collectors.joining(","));
                }
                cs.beginText();
                cs.newLineAtOffset(x, y);
                cs.showText(documento);
                cs.endText();

                y -= LINE_HEIGHT;
                
                // Actualizar contexto
                ctx = new PdfContext(ctx.getDocument(), ctx.getCurrentPage(), cs, y,
                                     ctx.getBold(), ctx.getRegular(), ctx.getSmall(),
                                     ctx.getPageHeight(), ctx.getPageWidth());
            }
        }

        y -= 10;
        return new PdfContext(ctx.getDocument(), ctx.getCurrentPage(), ctx.getCs(), y,
                              ctx.getBold(), ctx.getRegular(), ctx.getSmall(),
                              ctx.getPageHeight(), ctx.getPageWidth());
    }

    // Método auxiliar para traducir claves de forma de pago
    private String getFormaPagoDescripcion(String clave) {
        if (clave == null) return "";
        switch (clave) {
            case "01": return "Efectivo";
            case "02": return "Cheque nominativo";
            case "03": return "Transferencia electrónica";
            case "04": return "Tarjeta de crédito";
            case "05": return "Monedero electrónico";
            case "06": return "Dinero electrónico";
            case "08": return "Vales de despensa";
            case "12": return "Dación en pago";
            case "13": return "Pago por subrogación";
            case "14": return "Pago por consignación";
            case "15": return "Condonación";
            case "17": return "Compensación";
            case "23": return "Novación";
            case "24": return "Confusión";
            case "25": return "Remisión de deuda";
            case "26": return "Prescripción o caducidad";
            case "27": return "A satisfacción del acreedor";
            case "28": return "Tarjeta de débito";
            case "29": return "Tarjeta de servicios";
            case "30": return "Aplicación de anticipos";
            case "31": return "Intermediario pagos";
            case "99": return "Otros";
            default: return clave;
        }
    }

    // ==================== CARGA DE FUENTES ====================

    private PDFont loadFont(PDDocument document, String fontPath) throws IOException {
        try {
            File fontFile = new File(fontPath);
            if (fontFile.exists()) {
                return PDType0Font.load(document, fontFile);
            }
        } catch (Exception e) {
            // Fallback a fuente estándar
        }
        return PDType1Font.HELVETICA;
    }
}