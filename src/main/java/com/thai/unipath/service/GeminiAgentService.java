package com.thai.unipath.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thai.unipath.StudentController.NganhHoc;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

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
                "Lưu ý: Nói chuyện tự nhiên, genZ, xưng 'mình' gọi 'bạn'. Tuyệt đối không bịa thông tin trường nếu không có trong dữ liệu."
        })
        String chat(String userMessage);
    }

    private TuVanTuyenSinhAgent agent;

    public GeminiAgentService() {
        ChatLanguageModel model = GoogleAiGeminiChatModel.builder()
                .apiKey("AIzaSyAbu2EdJEPh1taKVhOJvdUnPQjhryfvYTc")
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

        @Tool("Tính tất cả các khối xét tuyển và điểm tương ứng từ điểm thi. Truyền null nếu môn đó học sinh không thi hoặc không nhắc đến.")
        public String tinhToanTatCaCacKhoi(Double toan, Double van, Double anh, Double ly, Double hoa, Double sinh, Double su, Double dia, Double gdkt, Double tin) {
            System.out.println("🤖 AI đang tự động phân tích tổ hợp khối...");
            Map<String, Double> khoiHopLe = new HashMap<>();

            boolean t = toan != null && toan > 0; boolean v = van != null && van > 0;
            boolean a = anh != null && anh > 0;   boolean l = ly != null && ly > 0;
            boolean h = hoa != null && hoa > 0;   boolean s = sinh != null && sinh > 0;
            boolean su_ = su != null && su > 0;   boolean d = dia != null && dia > 0;
            boolean ti = tin != null && tin > 0;

            if (t && l && h) khoiHopLe.put("A00", toan + ly + hoa);
            if (t && l && a) khoiHopLe.put("A01", toan + ly + anh);
            if (t && h && s) khoiHopLe.put("B00", toan + hoa + sinh);
            if (v && su_ && d) khoiHopLe.put("C00", van + su + dia);
            if (t && v && l) khoiHopLe.put("C01", toan + van + ly);
            if (t && v && h) khoiHopLe.put("C02", toan + van + hoa);
            if (v && l && h) khoiHopLe.put("C05", van + ly + hoa);
            if (t && v && a) khoiHopLe.put("D01", toan + van + anh);
            if (t && l && ti) khoiHopLe.put("A10", toan + ly + tin);

            if (khoiHopLe.isEmpty()) return "Không thể tổ hợp thành khối nào từ các môn đã cho.";

            StringBuilder result = new StringBuilder("Các khối hợp lệ:\n");
            khoiHopLe.forEach((khoi, diem) -> result.append("- Khối ").append(khoi).append(": ").append(diem).append(" điểm\n"));
            return result.toString();
        }

        @Tool("Quét file JSON tìm các trường/ngành có điểm chuẩn <= điểm thi của học sinh. Trả về top 5 lựa chọn tốt nhất.")
        public String traCuuCoHoiTrungTuyen(String tenKhoi, double diemCuaThiSinh) {
            System.out.println("🤖 AI đang quét JSON tìm cơ hội đỗ cho khối " + tenKhoi + " với " + diemCuaThiSinh + " điểm...");
            if (khoDuLieuNganh == null || khoDuLieuNganh.isEmpty()) return "Lỗi: Không load được dữ liệu JSON.";

            List<NganhHoc> phuHop = khoDuLieuNganh.stream()
                    .filter(n -> n.khoi != null && n.khoi.contains(tenKhoi))
                    .filter(n -> n.diemChuan > 0 && diemCuaThiSinh >= n.diemChuan) // Lọc các trường đủ điểm đỗ
                    .sorted((n1, n2) -> Double.compare(n2.diemChuan, n1.diemChuan)) // Ưu tiên trường lấy điểm cao (trường xịn) sát mức điểm của thí sinh
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