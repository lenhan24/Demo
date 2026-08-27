package utilities;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

/** Xuất danh sách ra file CSV (Excel mở trực tiếp được), không cần thêm thư viện. */
public class ExcelExportUtil {

    public static void exportCsv(HttpServletResponse response, String fileName,
                                  String[] headers, List<String[]> rows) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        OutputStream os = response.getOutputStream();
        os.write(0xEF); os.write(0xBB); os.write(0xBF); // BOM để Excel đọc đúng tiếng Việt

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.println(String.join(",", headers));
        for (String[] row : rows) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < row.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(escape(row[i]));
            }
            writer.println(sb.toString());
        }
        writer.flush();
    }

    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
