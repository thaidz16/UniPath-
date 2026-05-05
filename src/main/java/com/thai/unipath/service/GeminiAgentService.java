package com.thai.unipath.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thai.unipath.StudentController.NganhHoc;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import org.apache.logging.log4j.util.BiConsumer;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class GeminiAgentService {

    public interface TuVanTuyenSinhAgent {
        @SystemMessage({
                "Bạn là UniBot - Chuyên gia tư vấn tuyển sinh đại học thông minh.",
                "QUY TRÌNH LÀM VIỆC BẮT BUỘC KHI HỌC SINH NHẬP ĐIỂM:",
                "Bước 1: Gọi công cụ 'tinhToanTatCaCacKhoi' để xác định tất cả các khối hợp lệ và điểm số tương ứng.",
                "Bước 2: Phân tích xem khối nào có điểm số cao nhất hoặc lợi thế nhất.",
                "Bước 3: Gọi công cụ 'traCuuCoHoiTrungTuyen' ĐỂ QUÉT FILE JSON cho 1 hoặc 2 khối có điểm cao nhất.",
                "Bước 4: Trả lời học sinh: Liệt kê các khối họ có thể xét tuyển, gợi ý khối có xác suất đỗ cao nhất dựa trên dữ liệu thật, và recommend top các trường/ngành phù hợp.",
                "Lưu ý: Nói chuyện tự nhiên, genZ, xưng 'mình' gọi 'bạn'. Tuyệt đối không bịa thông tin trường nếu không có trong dữ liệu.",
                "QUY TẮC TỐI THƯỢNG CẦN TUÂN THỦ",
                "1. CHỈ được phép tư vấn dựa trên CHÍNH XÁC các môn học và điểm số mà người dùng cung cấp.",
                "2. TUYỆT ĐỐI KHÔNG TỰ BỊA ĐẶT, KHÔNG TỰ THÊM THẮT bất kỳ môn học nào (như Lý, Hóa, Anh...) nếu người dùng không nhập.",
                "3. TUYỆT ĐỐI KHÔNG tự tính toán điểm khối thi nếu thiếu môn.",
                "4. Nếu các môn người dùng nhập không thể ghép thành khối thi đại học chuẩn (VD: Toán, Văn, Tin, GDCD không tạo thành khối phổ biến), hãy trả lời trung thực: \"Với các môn bạn cung cấp, hiện hệ thống chưa tìm thấy tổ hợp xét tuyển truyền thống nào phù hợp. Bạn có thi thêm môn nào khác không?"


        })
        String chat(String userMessage);
    }

    private TuVanTuyenSinhAgent agent;

    public GeminiAgentService() {
        ChatLanguageModel model = GoogleAiGeminiChatModel.builder()
                .apiKey("AIzaSyB00M6qDSs37Vs4mMWPYcnMvzz-jm_OM40")
                .modelName("gemini-2.5-flash")
                .build();

        this.agent = AiServices.builder(TuVanTuyenSinhAgent.class)
                .chatLanguageModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(15))
                .tools(new TuyenSinhTools())
                .build();
    }

    public String hoiBot(String cauHoi) {
        return agent.chat(cauHoi);
    }

    static class TuyenSinhTools {
        private List<NganhHoc> khoDuLieuNganh = new ArrayList<>();

        public TuyenSinhTools() {
            try (Reader reader = new FileReader("nganh_hoc.json")) {
                Type listType = new TypeToken<ArrayList<NganhHoc>>(){}.getType();
                khoDuLieuNganh = new Gson().fromJson(reader, listType);
                System.out.println("✅ Nạp đạn thành công! Đã load xong dữ liệu từ gốc.");
            } catch (Exception e) {
                System.err.println("❌ Vẫn không thấy file! Thư mục code đang chạy thực tế là: " + System.getProperty("user.dir"));
                e.printStackTrace();
            }
        }

        @Tool("Tính tất cả các khối xét tuyển và điểm tương ứng từ điểm thi. TUYỆT ĐỐI CHỈ DÙNG CÁC MÔN NGƯỜI DÙNG CUNG CẤP. BẮT BUỘC truyền null cho TẤT CẢ các môn mà người dùng KHÔNG NHẮC ĐẾN. CẤM TỰ BỊA ĐIỂM HOẶC THÊM MÔN THI.")
        public String tinhToanTatCaCacKhoi(@P("Điểm Toán. BẮT BUỘC truyền null nếu không có") Double toan,
                                           @P("Điểm Văn. BẮT BUỘC truyền null nếu không có") Double van,
                                           @P("Điểm Tiếng Anh. BẮT BUỘC truyền null nếu không có") Double anh,
                                           @P("Điểm Vật Lý. BẮT BUỘC truyền null nếu không có") Double ly,
                                           @P("Điểm Hóa Học. BẮT BUỘC truyền null nếu không có") Double hoa,
                                           @P("Điểm Sinh Học. BẮT BUỘC truyền null nếu không có") Double sinh,
                                           @P("Điểm Tin Học. BẮT BUỘC truyền null nếu không có") Double tin,
                                           @P("Điểm GDKT (hoặc GDCD). BẮT BUỘC truyền null nếu không có") Double gdkt,
                                           @P("Điểm Lịch Sử. BẮT BUỘC truyền null nếu không có") Double su,
                                           @P("Điểm Địa Lý. BẮT BUỘC truyền null nếu không có") Double dia
        ) {
            System.out.println("🤖 AI đang tự động phân tích tổ hợp khối...");

            boolean t  = toan != null && toan > 0;
            boolean v  = van  != null && van  > 0;
            boolean a  = anh  != null && anh  > 0;
            boolean l  = ly   != null && ly   > 0;
            boolean h  = hoa  != null && hoa  > 0;
            boolean s  = sinh != null && sinh > 0;
            boolean su_= su   != null && su   > 0;
            boolean d  = dia  != null && dia  > 0;
            boolean g  = gdkt != null && gdkt > 0;
            boolean ti = tin  != null && tin  > 0;

            // Ghi nhận 1 tổ hợp: chỉ add khi cả 3 môn đều hợp lệ
            record KhoiEntry(String ma, double diem, String monThi) {}
            List<KhoiEntry> danhSach = new ArrayList<>();

            BiConsumer<String[], double[]> add = (info, scores) -> {
                // info[0]=mã khối, info[1]=tên 3 môn
                danhSach.add(new KhoiEntry(info[0], scores[0] + scores[1] + scores[2], info[1]));
            };

            // khối A
            if (t && l && h)   add.accept(new String[]{"A00","Toán + Lý + Hóa"},        new double[]{toan,ly,hoa});
            if (t && l && a)   add.accept(new String[]{"A01","Toán + Lý + Anh"},         new double[]{toan,ly,anh});
            if (t && l && s)   add.accept(new String[]{"A02","Toán + Lý + Sinh"},        new double[]{toan,ly,sinh});
            if (t && l && su_) add.accept(new String[]{"A03","Toán + Lý + Sử"},          new double[]{toan,ly,su});
            if (t && l && d)   add.accept(new String[]{"A04","Toán + Lý + Địa"},         new double[]{toan,ly,dia});
            if (t && h && su_) add.accept(new String[]{"A05","Toán + Hóa + Sử"},         new double[]{toan,hoa,su});
            if (t && h && d)   add.accept(new String[]{"A06","Toán + Hóa + Địa"},        new double[]{toan,hoa,dia});
            if (t && su_&& d)  add.accept(new String[]{"A07","Toán + Sử + Địa"},         new double[]{toan,su,dia});
            if (t && su_&& g)  add.accept(new String[]{"A08","Toán + Sử + GDKT"},        new double[]{toan,su,gdkt});
            if (t && d && g)   add.accept(new String[]{"A09","Toán + Địa + GDKT"},       new double[]{toan,dia,gdkt});
            if (t && l && ti)  add.accept(new String[]{"A10","Toán + Lý + Tin"},         new double[]{toan,ly,tin});
            if (t && h && ti)  add.accept(new String[]{"A11","Toán + Hóa + Tin"},        new double[]{toan,hoa,tin});

            // KHỐI B
            if (t && h && s)   add.accept(new String[]{"B00","Toán + Hóa + Sinh"},       new double[]{toan,hoa,sinh});
            if (t && s && su_) add.accept(new String[]{"B01","Toán + Sinh + Sử"},        new double[]{toan,sinh,su});
            if (t && s && d)   add.accept(new String[]{"B02","Toán + Sinh + Địa"},       new double[]{toan,sinh,dia});
            if (t && s && v)   add.accept(new String[]{"B03","Toán + Sinh + Văn"},       new double[]{toan,sinh,van});
            if (t && s && g)   add.accept(new String[]{"B04","Toán + Sinh + GDKT"},      new double[]{toan,sinh,gdkt});
            if (t && s && a)   add.accept(new String[]{"B08","Toán + Sinh + Anh"},       new double[]{toan,sinh,anh});

            // KHỐI C
            if (v && su_&& d)  add.accept(new String[]{"C00","Văn + Sử + Địa"},          new double[]{van,su,dia});
            if (t && v && l)   add.accept(new String[]{"C01","Toán + Văn + Lý"},         new double[]{toan,van,ly});
            if (t && v && h)   add.accept(new String[]{"C02","Toán + Văn + Hóa"},        new double[]{toan,van,hoa});
            if (t && v && su_) add.accept(new String[]{"C03","Toán + Văn + Sử"},         new double[]{toan,van,su});
            if (t && v && d)   add.accept(new String[]{"C04","Toán + Văn + Địa"},        new double[]{toan,van,dia});
            if (v && l && h)   add.accept(new String[]{"C05","Văn + Lý + Hóa"},          new double[]{van,ly,hoa});
            if (v && l && s)   add.accept(new String[]{"C06","Văn + Lý + Sinh"},         new double[]{van,ly,sinh});
            if (v && l && su_) add.accept(new String[]{"C07","Văn + Lý + Sử"},           new double[]{van,ly,su});
            if (v && h && s)   add.accept(new String[]{"C08","Văn + Hóa + Sinh"},        new double[]{van,hoa,sinh});
            if (v && l && d)   add.accept(new String[]{"C09","Văn + Lý + Địa"},          new double[]{van,ly,dia});
            if (v && h && su_) add.accept(new String[]{"C10","Văn + Hóa + Sử"},          new double[]{van,hoa,su});
            if (v && s && su_) add.accept(new String[]{"C12","Văn + Sinh + Sử"},         new double[]{van,sinh,su});
            if (v && s && d)   add.accept(new String[]{"C13","Văn + Sinh + Địa"},        new double[]{van,sinh,dia});
            if (t && v && g)   add.accept(new String[]{"C14","Toán + Văn + GDKT"},       new double[]{toan,van,gdkt});
            if (v && l && g)   add.accept(new String[]{"C16","Văn + Lý + GDKT"},         new double[]{van,ly,gdkt});
            if (v && h && g)   add.accept(new String[]{"C17","Văn + Hóa + GDKT"},        new double[]{van,hoa,gdkt});
            if (v && s && g)   add.accept(new String[]{"C18","Văn + Sinh + GDKT"},       new double[]{van,sinh,gdkt});
            if (v && su_&& g)  add.accept(new String[]{"C19","Văn + Sử + GDKT"},         new double[]{van,su,gdkt});
            if (v && d && g)   add.accept(new String[]{"C20","Văn + Địa + GDKT"},        new double[]{van,dia,gdkt});

            // KHỐI D
            if (t && v && a)   add.accept(new String[]{"D01","Toán + Văn + Anh"},        new double[]{toan,van,anh});
            if (t && h && a)   add.accept(new String[]{"D07","Toán + Hóa + Anh"},        new double[]{toan,hoa,anh});
            if (t && s && a)   add.accept(new String[]{"D08","Toán + Sinh + Anh"},       new double[]{toan,sinh,anh});
            if (t && su_&& a)  add.accept(new String[]{"D09","Toán + Sử + Anh"},         new double[]{toan,su,anh});
            if (t && d && a)   add.accept(new String[]{"D10","Toán + Địa + Anh"},        new double[]{toan,dia,anh});
            if (v && l && a)   add.accept(new String[]{"D11","Văn + Lý + Anh"},          new double[]{van,ly,anh});
            if (v && h && a)   add.accept(new String[]{"D12","Văn + Hóa + Anh"},         new double[]{van,hoa,anh});
            if (v && s && a)   add.accept(new String[]{"D13","Văn + Sinh + Anh"},        new double[]{van,sinh,anh});
            if (v && su_&& a)  add.accept(new String[]{"D14","Văn + Sử + Anh"},          new double[]{van,su,anh});
            if (v && d && a)   add.accept(new String[]{"D15","Văn + Địa + Anh"},         new double[]{van,dia,anh});
            if (v && g && a)   add.accept(new String[]{"D66","Văn + GDKT + Anh"},        new double[]{van,gdkt,anh});
            if (t && v && ti)  add.accept(new String[]{"D10T","Toán + Văn + Tin"},       new double[]{toan,van,tin});
            if (t && a && ti)  add.accept(new String[]{"D90","Toán + Anh + Tin"},        new double[]{toan,anh,tin});

            if (danhSach.isEmpty())
                return "❌ Không thể tổ hợp thành khối nào từ các môn đã cho.";

            danhSach.sort(Comparator
                    .comparing((KhoiEntry e) -> e.ma().replaceAll("[^A-Z]", ""))
                    .thenComparing(e -> e.ma())
                    .thenComparingDouble(e -> -e.diem()));

            double diemCaoNhat = danhSach.stream().mapToDouble(KhoiEntry::diem).max().orElse(0);

            StringBuilder result = new StringBuilder();
            result.append("📊 Tổng hợp các khối xét tuyển hợp lệ (").append(danhSach.size()).append(" khối):\n");
            result.append("─".repeat(55)).append("\n");

            String nhomHienTai = "";
            for (KhoiEntry e : danhSach) {
                String nhom = e.ma().replaceAll("[^A-Z]", "");
                if (!nhom.equals(nhomHienTai)) {
                    if (!nhomHienTai.isEmpty()) result.append("\n");
                    result.append("▌ Khối ").append(nhom).append(":\n");
                    nhomHienTai = nhom;
                }
                boolean laCaoNhat = e.diem() == diemCaoNhat;
                result.append(String.format("  %-6s │ %-22s │ %5.2f điểm%s%n",
                        e.ma(), e.monThi(), e.diem(),
                        laCaoNhat ? " ⭐" : ""));
            }

            result.append("─".repeat(55)).append("\n");
            result.append(String.format("🏆 Khối điểm cao nhất: %.2f điểm%n", diemCaoNhat));

            return result.toString();
        }

        @Tool("Quét file JSON tìm các trường/ngành có điểm chuẩn <= điểm thi của học sinh. Trả về top 5 lựa chọn tốt nhất.")
        public String traCuuCoHoiTrungTuyen(String tenKhoi, double diemCuaThiSinh) {
            System.out.println("🤖 AI đang quét JSON tìm cơ hội đỗ cho khối " + tenKhoi + " với " + diemCuaThiSinh + " điểm...");
            if (khoDuLieuNganh == null || khoDuLieuNganh.isEmpty()) return "Lỗi: Không load được dữ liệu. ";

            List<NganhHoc> phuHop = khoDuLieuNganh.stream()
                    .filter(n -> n.khoi != null && n.khoi.contains(tenKhoi))
                    .filter(n -> n.diemChuan > 0 && diemCuaThiSinh >= n.diemChuan)
                    .sorted((n1, n2) -> Double.compare(n2.diemChuan, n1.diemChuan))
                    .limit(5)
                    .collect(Collectors.toList());

            if (phuHop.isEmpty()) return "Với " + diemCuaThiSinh + " điểm khối " + tenKhoi + ", hiện tại chưa tìm thấy ngành nào an toàn trong dữ liệu.";

            StringBuilder sb = new StringBuilder("Top lựa chọn an toàn cho khối " + tenKhoi + " (" + diemCuaThiSinh + " điểm):\n");
            for (NganhHoc n : phuHop) {
                sb.append("- Trường: ").append(n.tenTruong).append(" | Ngành: ").append(n.tenNganh)
                        .append(" (Điểm chuẩn năm ngoái: ").append(n.diemChuan).append(")\n");
            }
            return sb.toString();
        }
    }
}