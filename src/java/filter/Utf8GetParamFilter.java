package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * MỚI: sửa lỗi "tìm kiếm tiếng Việt không ra kết quả" ở các form dùng
 * method="get" (VD ô tìm kiếm sản phẩm ở Manager, tìm kiếm đơn hàng ở
 * Staff...). Nguyên nhân gốc: request.setCharacterEncoding("UTF-8") CHỈ ảnh
 * hưởng dữ liệu POST — với tham số GET (query string trên URL), việc giải
 * mã ký tự do tầng container (Tomcat) xử lý ngay khi nhận request, XONG
 * TRƯỚC KHI code Servlet chạy, dựa theo URIEncoding cấu hình ở server —
 * không sửa được bằng code trong Servlet.
 *
 * Filter này chặn MỌI request GET, tự đọc lại query string thô rồi giải mã
 * đúng UTF-8, bọc lại thành 1 HttpServletRequestWrapper để getParameter()
 * trả về đúng ký tự có dấu — không phụ thuộc cấu hình URIEncoding của server
 * đang chạy (chắc chắn hoạt động dù deploy ở máy nào).
 */
@WebFilter(filterName = "Utf8GetParamFilter", urlPatterns = {"/*"})
public class Utf8GetParamFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;

        if ("GET".equalsIgnoreCase(httpReq.getMethod()) && httpReq.getQueryString() != null) {
            chain.doFilter(new Utf8QueryStringWrapper(httpReq), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

    /**
     * Bọc lại request GET — parse thủ công query string thô, giải mã đúng
     * UTF-8, override getParameter()/getParameterValues()/getParameterMap()
     * để trả về đúng giá trị đã giải mã, thay vì để container tự giải mã sai.
     */
    private static class Utf8QueryStringWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {

        private final java.util.Map<String, String[]> fixedParams = new java.util.LinkedHashMap<>();

        Utf8QueryStringWrapper(HttpServletRequest request) {
            super(request);
            String query = request.getQueryString();
            if (query == null || query.isEmpty()) {
                return;
            }
            for (String pair : query.split("&")) {
                if (pair.isEmpty()) {
                    continue;
                }
                int eq = pair.indexOf('=');
                String rawKey = (eq >= 0) ? pair.substring(0, eq) : pair;
                String rawVal = (eq >= 0) ? pair.substring(eq + 1) : "";
                String key = decode(rawKey);
                String val = decode(rawVal);

                String[] existing = fixedParams.get(key);
                if (existing == null) {
                    fixedParams.put(key, new String[]{val});
                } else {
                    String[] merged = java.util.Arrays.copyOf(existing, existing.length + 1);
                    merged[existing.length] = val;
                    fixedParams.put(key, merged);
                }
            }
        }

        private String decode(String s) {
            try {
                return java.net.URLDecoder.decode(s, "UTF-8");
            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                return s; // chuỗi không decode được (hiếm) -> trả về nguyên bản, không chặn request
            }
        }

        @Override
        public String getParameter(String name) {
            String[] vals = fixedParams.get(name);
            return (vals != null && vals.length > 0) ? vals[0] : super.getParameter(name);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] vals = fixedParams.get(name);
            return (vals != null) ? vals : super.getParameterValues(name);
        }

        @Override
        public java.util.Map<String, String[]> getParameterMap() {
            if (fixedParams.isEmpty()) {
                return super.getParameterMap();
            }
            return java.util.Collections.unmodifiableMap(fixedParams);
        }
    }
}
