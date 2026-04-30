package com.thai.unipath;

import com.thai.unipath.model.Diem;
import com.thai.unipath.model.ThiSinh;
import com.thai.unipath.service.ExcelServiceImpl;
import com.thai.unipath.service.GeminiAgentService;
import com.thai.unipath.service.IDataService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.Desktop;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class StudentController {

    //  KHAI BÁO UI COMPONENT
    @FXML private Label lblDate, lblHoTen, lblNgaySinh, lblQueQuan, lblTongDiem, lblDiemChiTiet, lblBadge, lblTitleGoiY;
    @FXML private TextField txtSBD, txtSearchTruong;
    @FXML private VBox resultArea, viewTraCacDiem, viewTraDaiHoc, suggestionBox, listTruongGoiY;
    @FXML private FlowPane boxKhoi;
    @FXML private Label menuTraDiem, menuTraDaiHoc;
    @FXML private Button btnHN, btnHCM, btnDN, btnCT, btnKhac;
    @FXML private Accordion accordionTruong, accordionNganh;
    @FXML private RadioButton rbDaiHoc, rbCaoDang;
    @FXML private Button btnLoginAdmin;
    @FXML private ScrollPane scrollChat;
    @FXML private VBox boxChatHistory;
    @FXML private TextField txtChatInput;
    @FXML private VBox boxTitleSearch;
    @FXML private VBox chatWindow;
    @FXML private Button btnCallBot;

    // UI TAB 3 (TÌM THEO ĐIỂM)
    @FXML private TextField txtDiemMin, txtDiemMax, txtSearchDiem;
    @FXML private Slider sliderDiemMin, sliderDiemMax;
    @FXML private ComboBox<String> cbKhuVucDiem, cbHeDiem, cbKhoiDiem, cbNhomDiem, cbSortDiem;
    @FXML private Label lblCountDiem;
    @FXML private TableView<LocResult> tableDiem;
    @FXML private ListView<String> listNhomNganh;
    @FXML private TableView<NganhHoc> tableNganhChiTiet;

    //  UI TAB 4 (TÌM THEO HỌC PHÍ)
    @FXML private TextField txtHpMin, txtHpMax, txtSearchHp;
    @FXML private Slider sliderHpMin, sliderHpMax;
    @FXML private ComboBox<String> cbKhuVucHp, cbHeHp, cbKhoiHp, cbNhomHp, cbSortHp;
    @FXML private Label lblCountHp;
    @FXML private TableView<LocResult> tableHp;

    @FXML
    protected void onOpenLogin() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void toggleChatWindow() {
        boolean isNowVisible = !chatWindow.isVisible();
        chatWindow.setVisible(isNowVisible);
        btnCallBot.setVisible(!isNowVisible);
    }

    // DATA CLASSES
    public static class TruongDH {
        String ma, ten, khuVuc, diaChi, sdt, web, trangThai, mauTrangThai, heDaoTao;
        List<String> nganhDaoTao;
    }

    public static class NganhHoc {
        public String maNganh, tenNganh, nhomNganh, khoi, tenTruong, heHoc, moTaNganh;
        public double diemChuan, hocPhi;
    }

    public static class LocResult {
        int stt; String maVaTenNganh; double diem; String khoi; double hocPhi; String truong;
        public LocResult(int stt, String m, double d, String k, double hp, String tr) {
            this.stt = stt; this.maVaTenNganh = m; this.diem = d; this.khoi = k; this.hocPhi = hp; this.truong = tr;
        }
    }

    private List<TruongDH> khoDuLieuTruong = new ArrayList<>();
    private List<NganhHoc> khoDuLieuNganh = new ArrayList<>();
    private Button btnKhuVucDangChon;
    private IDataService dataService = new ExcelServiceImpl();
    private List<ThiSinh> danhSachThiSinh;
    private DecimalFormat moneyFormat = new DecimalFormat("#,### VNĐ");
    private GeminiAgentService uniBot = new GeminiAgentService();

    @FXML
    public void initialize() {
        danhSachThiSinh = dataService.readData("DuLieu.xlsx");
        LocalDate now = LocalDate.now();
        String thu = switch (now.getDayOfWeek()) {
            case MONDAY -> "Thứ Hai"; case TUESDAY -> "Thứ Ba"; case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm"; case FRIDAY -> "Thứ Sáu"; case SATURDAY -> "Thứ Bảy"; case SUNDAY -> "Chủ Nhật";
        };
        lblDate.setText(thu + ", " + now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));        Gson gson = new Gson();

        try (Reader reader = new FileReader("truong_dai_hoc.json")) {
            Type listType = new TypeToken<ArrayList<TruongDH>>(){}.getType();
            khoDuLieuTruong = gson.fromJson(reader, listType);
        } catch (Exception e) {}

        try (Reader reader = new FileReader("nganh_hoc.json")) {
            Type listType = new TypeToken<ArrayList<NganhHoc>>(){}.getType();
            khoDuLieuNganh = gson.fromJson(reader, listType);
        } catch (Exception e) {}

        setupTabTruong();
        setupTabNganh();
        setupTabDiem();
        setupTabHocPhi();
    }

    // Helped
    private void initTableColumns(TableView<LocResult> table) {
        TableColumn<LocResult, Integer> colStt = new TableColumn<>("STT");
        colStt.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().stt)); colStt.setPrefWidth(50);

        TableColumn<LocResult, String> colNganh = new TableColumn<>("Tên, mã ngành");
        colNganh.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().maVaTenNganh)); colNganh.setPrefWidth(250);

        TableColumn<LocResult, String> colDiem = new TableColumn<>("Điểm chuẩn");
        colDiem.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().diem))); colDiem.setPrefWidth(100);

        TableColumn<LocResult, String> colKhoi = new TableColumn<>("Tổ hợp môn");
        colKhoi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().khoi)); colKhoi.setPrefWidth(100);

        TableColumn<LocResult, String> colHocPhi = new TableColumn<>("Học phí (VNĐ)");
        colHocPhi.setCellValueFactory(c -> new SimpleStringProperty(moneyFormat.format(c.getValue().hocPhi * 1000000))); colHocPhi.setPrefWidth(150);

        TableColumn<LocResult, String> colTruong = new TableColumn<>("Tên trường");
        colTruong.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().truong)); colTruong.setPrefWidth(250);

        table.getColumns().addAll(colStt, colNganh, colDiem, colKhoi, colHocPhi, colTruong);
    }

    private String getKhuVucNhanh(String tenTruong) {
        String t = tenTruong.toLowerCase();
        if(t.contains("hcm") || t.contains("tp.hcm")) return "TP HCM";
        if(t.contains("đà nẵng")) return "Đà Nẵng";
        return "Hà Nội";
    }

    // Tìm theo điểm
    private void setupTabDiem() {
        if(tableDiem == null) return;
        initTableColumns(tableDiem);

        cbKhuVucDiem.getItems().addAll("Tất cả", "Hà Nội", "TP HCM", "Đà Nẵng", "Cần Thơ", "Tỉnh thành khác"); cbKhuVucDiem.getSelectionModel().selectFirst();
        cbHeDiem.getItems().addAll("Tất cả", "Đại học", "Cao đẳng"); cbHeDiem.getSelectionModel().selectFirst();
        cbKhoiDiem.getItems().addAll("Tất cả các khối", "A00", "A01", "B00", "C00", "D01"); cbKhoiDiem.getSelectionModel().selectFirst();
        cbNhomDiem.getItems().addAll("Tất cả", "Máy tính và Công nghệ thông tin", "Kinh doanh và quản lý", "Ngôn ngữ", "Kỹ thuật", "Sức khỏe"); cbNhomDiem.getSelectionModel().selectFirst();
        cbSortDiem.getItems().addAll("Điểm chuẩn thấp -> cao", "Điểm chuẩn cao -> thấp", "Tên A-Z"); cbSortDiem.getSelectionModel().selectFirst();

        sliderDiemMin.valueProperty().addListener((obs, o, n) -> { txtDiemMin.setText(String.format("%.0f", n)); updateTableDiem(); });
        sliderDiemMax.valueProperty().addListener((obs, o, n) -> { txtDiemMax.setText(String.format("%.0f", n)); updateTableDiem(); });

        cbKhuVucDiem.valueProperty().addListener((o, oldV, newV) -> updateTableDiem());
        cbHeDiem.valueProperty().addListener((o, oldV, newV) -> updateTableDiem());
        cbKhoiDiem.valueProperty().addListener((o, oldV, newV) -> updateTableDiem());
        cbNhomDiem.valueProperty().addListener((o, oldV, newV) -> updateTableDiem());
        cbSortDiem.valueProperty().addListener((o, oldV, newV) -> updateTableDiem());
        txtSearchDiem.textProperty().addListener((o, oldV, newV) -> updateTableDiem());

        updateTableDiem();
    }

    private void updateTableDiem() {
        List<LocResult> list = new ArrayList<>();
        double min = sliderDiemMin.getValue(); double max = sliderDiemMax.getValue();
        String he = cbHeDiem.getValue(); String kv = cbKhuVucDiem.getValue();
        String khoi = cbKhoiDiem.getValue(); String nhom = cbNhomDiem.getValue();
        String search = txtSearchDiem.getText().trim().toLowerCase();

        for(NganhHoc n : khoDuLieuNganh) {
            if(n.diemChuan < min || n.diemChuan > max) continue;
            if(!"Tất cả".equals(he) && (n.heHoc == null || !n.heHoc.equals(he))) continue;
            if(!"Tất cả các khối".equals(khoi) && (n.khoi == null || !n.khoi.contains(khoi))) continue;
            if(!"Tất cả".equals(nhom) && !n.nhomNganh.equals(nhom)) continue;
            if(!search.isEmpty() && !n.tenNganh.toLowerCase().contains(search) && !n.maNganh.toLowerCase().contains(search)) continue;

            if(n.tenTruong != null) {
                String truong = n.tenTruong;
                if(!"Tất cả".equals(kv) && !getKhuVucNhanh(truong).equals(kv)) continue;
                list.add(new LocResult(0, n.tenNganh + "\n" + n.maNganh, n.diemChuan, n.khoi != null ? n.khoi : "", n.hocPhi, truong));            }
        }

        if("Điểm chuẩn thấp -> cao".equals(cbSortDiem.getValue())) list.sort((a,b) -> Double.compare(a.diem, b.diem));
        else if("Điểm chuẩn cao -> thấp".equals(cbSortDiem.getValue())) list.sort((a,b) -> Double.compare(b.diem, a.diem));
        else list.sort((a,b) -> a.maVaTenNganh.compareToIgnoreCase(b.maVaTenNganh));

        for(int i = 0; i < list.size(); i++) list.get(i).stt = i + 1;
        lblCountDiem.setText(list.size() + " Kết quả");
        tableDiem.setItems(FXCollections.observableArrayList(list));
    }

    private void setupTabHocPhi() {
        if(tableHp == null) return;
        initTableColumns(tableHp);

        cbKhuVucHp.getItems().addAll("Tất cả", "Hà Nội", "TP HCM", "Đà Nẵng", "Cần Thơ", "Tỉnh thành khác"); cbKhuVucHp.getSelectionModel().selectFirst();
        cbHeHp.getItems().addAll("Tất cả", "Đại học", "Cao đẳng"); cbHeHp.getSelectionModel().selectFirst();
        cbKhoiHp.getItems().addAll("Tất cả các khối", "A00", "A01", "B00", "C00", "D01"); cbKhoiHp.getSelectionModel().selectFirst();
        cbNhomHp.getItems().addAll("Tất cả", "Máy tính và Công nghệ thông tin", "Kinh doanh và quản lý", "Ngôn ngữ", "Kỹ thuật", "Sức khỏe"); cbNhomHp.getSelectionModel().selectFirst();
        cbSortHp.getItems().addAll("Học phí thấp -> cao", "Học phí cao -> thấp", "Tên A-Z"); cbSortHp.getSelectionModel().selectFirst();

        sliderHpMin.valueProperty().addListener((obs, o, n) -> { txtHpMin.setText(String.format("%.0f", n)); updateTableHp(); });
        sliderHpMax.valueProperty().addListener((obs, o, n) -> { txtHpMax.setText(String.format("%.0f", n)); updateTableHp(); });

        cbKhuVucHp.valueProperty().addListener((o, oldV, newV) -> updateTableHp());
        cbHeHp.valueProperty().addListener((o, oldV, newV) -> updateTableHp());
        cbKhoiHp.valueProperty().addListener((o, oldV, newV) -> updateTableHp());
        cbNhomHp.valueProperty().addListener((o, oldV, newV) -> updateTableHp());
        cbSortHp.valueProperty().addListener((o, oldV, newV) -> updateTableHp());
        txtSearchHp.textProperty().addListener((o, oldV, newV) -> updateTableHp());

        updateTableHp();
    }

    private void updateTableHp() {
        List<LocResult> list = new ArrayList<>();
        double min = sliderHpMin.getValue(); double max = sliderHpMax.getValue();
        String he = cbHeHp.getValue(); String kv = cbKhuVucHp.getValue();
        String khoi = cbKhoiHp.getValue(); String nhom = cbNhomHp.getValue();
        String search = txtSearchHp.getText().trim().toLowerCase();

        for(NganhHoc n : khoDuLieuNganh) {
            if(!"Tất cả".equals(he) && (n.heHoc == null || !n.heHoc.equals(he))) continue;
            if(!"Tất cả các khối".equals(khoi) && (n.khoi == null || !n.khoi.contains(khoi))) continue;
            if(!"Tất cả".equals(nhom) && !n.nhomNganh.equals(nhom)) continue;
            if(!search.isEmpty() && !n.tenNganh.toLowerCase().contains(search) && !n.maNganh.toLowerCase().contains(search)) continue;

            if(n.tenTruong != null) {
                String truong = n.tenTruong;
                if(!"Tất cả".equals(kv) && !getKhuVucNhanh(truong).equals(kv)) continue;
                double hp = n.hocPhi;
                if(hp >= min && hp <= max) {
                    list.add(new LocResult(0, n.tenNganh + "\n" + n.maNganh, n.diemChuan, n.khoi != null ? n.khoi : "", hp, truong));
                }
            }
        }

        if("Học phí thấp -> cao".equals(cbSortHp.getValue())) list.sort((a,b) -> Double.compare(a.hocPhi, b.hocPhi));
        else if("Học phí cao -> thấp".equals(cbSortHp.getValue())) list.sort((a,b) -> Double.compare(b.hocPhi, a.hocPhi));
        else list.sort((a,b) -> a.maVaTenNganh.compareToIgnoreCase(b.maVaTenNganh));

        for(int i = 0; i < list.size(); i++) list.get(i).stt = i + 1;
        lblCountHp.setText(list.size() + " Kết quả");
        tableHp.setItems(FXCollections.observableArrayList(list));
    }


    private void setupTabTruong() {
        btnKhuVucDangChon = btnHN;
        triggerFilterTruong();
        if (txtSearchTruong != null) txtSearchTruong.textProperty().addListener((obs, oldV, newV) -> triggerFilterTruong());
    }

    @FXML protected void onKhuVucClick(ActionEvent event) {
        btnKhuVucDangChon.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 5 15;");
        btnKhuVucDangChon = (Button) event.getSource();
        btnKhuVucDangChon.setStyle("-fx-background-color: #d35400; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 5 15;");
        triggerFilterTruong();
    }

    @FXML protected void triggerFilterTruong() {
        if (btnKhuVucDangChon != null && txtSearchTruong != null) hienThiDanhSachTruong(btnKhuVucDangChon.getText(), txtSearchTruong.getText().trim().toLowerCase());
    }

    private void hienThiDanhSachTruong(String khuVuc, String keyword) {
        if (accordionTruong == null) return;
        accordionTruong.getPanes().clear();
        String heCanTim = (rbDaiHoc != null && rbDaiHoc.isSelected()) ? "Đại học" : "Cao đẳng";
        for (TruongDH t : khoDuLieuTruong) {
            String heCuaTruong = (t.heDaoTao != null) ? t.heDaoTao : "Đại học";
            if (t.khuVuc.equals(khuVuc) && heCuaTruong.equals(heCanTim)) {
                if (keyword.isEmpty() || t.ten.toLowerCase().contains(keyword) || t.ma.toLowerCase().contains(keyword)) {
                    TitledPane tp = new TitledPane(); tp.setText("[" + t.ma + "] " + t.ten); tp.setTextFill(Color.web("#d35400")); tp.setFont(Font.font("System", FontWeight.BOLD, 15));
                    VBox content = new VBox(8); content.setStyle("-fx-background-color: #fdfefe; -fx-padding: 15; -fx-border-color: #ecf0f1; -fx-border-width: 0 1 1 1;");
                    Label lblTrangThai = new Label(t.trangThai);

                    lblTrangThai.setStyle("-fx-background-color: " + t.mauTrangThai +
                            "; -fx-text-fill: white;" +
                            " -fx-font-size: 11px;" +
                            " -fx-font-weight: bold;" +
                            " -fx-padding: 3 8;" +
                            " -fx-background-radius: 10;");

                    boxTitleSearch.getChildren().add(lblTrangThai);
                    content.getChildren().addAll(
                            new Label("📍 Địa chỉ: " + t.diaChi),
                            new Label("📞 Điện thoại: " + t.sdt),
                            new Label("🌐 Website: " + t.web),
                            lblTrangThai
                    );                    if (t.nganhDaoTao != null && !t.nganhDaoTao.isEmpty()) {
                        content.getChildren().add(new Separator()); Label lblNganhTitle = new Label("📚 Các ngành đào tạo nổi bật:"); lblNganhTitle.setTextFill(Color.web("#e67e22")); lblNganhTitle.setFont(Font.font("System", FontWeight.BOLD, 14)); content.getChildren().add(lblNganhTitle);
                        for (String nganh : t.nganhDaoTao) { Label lblNganh = new Label("   • " + nganh); lblNganh.setTextFill(Color.web("#34495e")); content.getChildren().add(lblNganh); }
                    }
                    tp.setContent(content); accordionTruong.getPanes().add(tp);
                }
            }
        }
        if (!accordionTruong.getPanes().isEmpty()) accordionTruong.setExpandedPane(accordionTruong.getPanes().get(0));
        else accordionTruong.getPanes().add(new TitledPane("❌ Không tìm thấy trường", new Label("Thử đổi khu vực hoặc hệ đào tạo.")));
    }

    private void setupTabNganh() {
        if (listNhomNganh == null || tableNganhChiTiet == null) return;

        java.util.List<String> nhomUnique = khoDuLieuNganh.stream()
                .map(n -> n.nhomNganh != null && !n.nhomNganh.isEmpty() ? n.nhomNganh : "Khác")
                .distinct().sorted().collect(java.util.stream.Collectors.toList());
        listNhomNganh.setItems(javafx.collections.FXCollections.observableArrayList(nhomUnique));

        tableNganhChiTiet.getColumns().clear();

        TableColumn<NganhHoc, String> colTen = new TableColumn<>("Tên, mã ngành");
        colTen.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().tenNganh + "\n" + cell.getValue().maNganh));
        colTen.setPrefWidth(220);

        TableColumn<NganhHoc, Double> colDiem = new TableColumn<>("Điểm chuẩn");
        colDiem.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().diemChuan));
        colDiem.setPrefWidth(100);

        TableColumn<NganhHoc, String> colKhoi = new TableColumn<>("Tổ hợp môn");
        colKhoi.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().khoi));
        colKhoi.setPrefWidth(150);
        colKhoi.setCellFactory(param -> new javafx.scene.control.TableCell<NganhHoc, String>() {
            private final javafx.scene.text.Text text = new javafx.scene.text.Text();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    text.wrappingWidthProperty().bind(colKhoi.widthProperty().subtract(10));
                    setGraphic(text);
                }
            }
        });

        TableColumn<NganhHoc, String> colHocPhi = new TableColumn<>("Học phí (VNĐ)");
        colHocPhi.setCellValueFactory(cell -> {
            java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
            return new javafx.beans.property.SimpleStringProperty(formatter.format(cell.getValue().hocPhi));
        });
        colHocPhi.setPrefWidth(130);

        TableColumn<NganhHoc, String> colTruong = new TableColumn<>("Tên trường");
        colTruong.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().tenTruong));
        colTruong.setPrefWidth(250);

        tableNganhChiTiet.getColumns().addAll(colTen, colDiem, colKhoi, colHocPhi, colTruong);
        tableNganhChiTiet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        listNhomNganh.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                java.util.List<NganhHoc> filtered = khoDuLieuNganh.stream()
                        .filter(n -> newVal.equals(n.nhomNganh != null && !n.nhomNganh.isEmpty() ? n.nhomNganh : "Khác"))
                        .collect(java.util.stream.Collectors.toList());
                tableNganhChiTiet.setItems(javafx.collections.FXCollections.observableArrayList(filtered));
            }
        });
    }

    private void updateDanhSachNganhCon(String nhom, VBox boxContainer, String selectedHe, String selectedKhoi, String selectedSort, String keyword) {
        boxContainer.getChildren().clear(); String keywordLower = keyword.trim().toLowerCase(); List<NganhHoc> filteredList = new ArrayList<>();
        for (NganhHoc n : khoDuLieuNganh) {
            if (!n.nhomNganh.equals(nhom)) continue;
            String heCuaNganh = (n.heHoc != null) ? n.heHoc : "Đại học"; String khoiCuaNganh = (n.khoi != null) ? n.khoi : "";
            boolean passHe = selectedHe.equals("Tất cả hệ") || heCuaNganh.equalsIgnoreCase(selectedHe);
            boolean passKhoi = selectedKhoi.equals("Tất cả khối") || khoiCuaNganh.contains(selectedKhoi);
            boolean passSearch = keywordLower.isEmpty() || n.tenNganh.toLowerCase().contains(keywordLower) || n.maNganh.toLowerCase().contains(keywordLower);
            if (passHe && passKhoi && passSearch) filteredList.add(n);
        }
        if (selectedSort != null) {
            switch (selectedSort) {
                case "Tên A-Z": filteredList.sort((a, b) -> a.tenNganh.compareToIgnoreCase(b.tenNganh)); break;
                case "Tên Z-A": filteredList.sort((a, b) -> b.tenNganh.compareToIgnoreCase(a.tenNganh)); break;
                case "Mã ngành A-Z": filteredList.sort((a, b) -> a.maNganh.compareToIgnoreCase(b.maNganh)); break;
                case "Điểm chuẩn: Cao - Thấp": filteredList.sort((a, b) -> Double.compare(b.diemChuan, a.diemChuan)); break;
                case "Điểm chuẩn: Thấp - Cao": filteredList.sort((a, b) -> Double.compare(a.diemChuan, b.diemChuan)); break;
            }
        }
        if (filteredList.isEmpty()) boxContainer.getChildren().add(new Label("❌ Không có ngành nào phù hợp với bộ lọc."));
        else {
            for (NganhHoc n : filteredList) {
                VBox cardNganh = new VBox(5); cardNganh.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 8; -fx-padding: 10; -fx-background-color: white;");
                Label lblTen = new Label("[" + n.maNganh + "] " + n.tenNganh); lblTen.setFont(Font.font("System", FontWeight.BOLD, 15)); lblTen.setTextFill(Color.web("#2980b9"));
                HBox boxInfo = new HBox(15); boxInfo.getChildren().addAll(new Label("🎓 Hệ: " + (n.heHoc != null ? n.heHoc : "Đại học")), new Label("📚 Khối: " + (n.khoi != null ? n.khoi : "Đang cập nhật")), new Label("🎯 Điểm chuẩn: " + n.diemChuan));
                cardNganh.getChildren().addAll(lblTen, boxInfo, new Label("💡 " + n.moTaNganh));
                if (n.tenTruong != null && !n.tenTruong.isEmpty()) {
                    cardNganh.getChildren().add(new Label("🏫 Trường đào tạo: " + n.tenTruong));
                }
                boxContainer.getChildren().add(cardNganh);
            }
        }
    }

    @FXML
    protected void switchViewToTraDiem() {
        viewTraCacDiem.setVisible(true); viewTraCacDiem.setManaged(true);
        viewTraDaiHoc.setVisible(false); viewTraDaiHoc.setManaged(false);
        menuTraDiem.setStyle("-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: #d35400; -fx-text-fill: #d35400;");
        menuTraDaiHoc.setStyle("-fx-cursor: hand; -fx-text-fill: #34495e; -fx-border-width: 0;");
    }

    @FXML
    protected void switchViewToTraDaiHoc() {
        viewTraDaiHoc.setVisible(true); viewTraDaiHoc.setManaged(true);
        viewTraCacDiem.setVisible(false); viewTraCacDiem.setManaged(false);
        menuTraDaiHoc.setStyle("-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: #d35400; -fx-text-fill: #d35400;");
        menuTraDiem.setStyle("-fx-cursor: hand; -fx-text-fill: #34495e; -fx-border-width: 0;");
    }

    @FXML
    protected void openFacebook() {
        try {
            Desktop.getDesktop().browse(new URI("https://www.facebook.com/TruongCongnghethongtinPhenikaa"));
        } catch (Exception e) {
            System.err.println("Lỗi mở Facebook: " + e.getMessage());
        }
    }

    @FXML
    protected void openInstagram() {
        try {
            Desktop.getDesktop().browse(new URI("https://www.instagram.com/cntt_phenikaa/"));
        } catch (Exception e) {
            System.err.println("Lỗi mở Instagram: " + e.getMessage());
        }
    }

    @FXML protected void onSearchClick() {
        String sbd = txtSBD.getText().trim();
        if (sbd.isEmpty()) return;

        ThiSinh tsFound = null;
        for (ThiSinh t : danhSachThiSinh) {
            String excelSbd = t.getSbd().trim().endsWith(".0") ? t.getSbd().trim().substring(0, t.getSbd().trim().length()-2) : t.getSbd().trim();
            if (excelSbd.equals(sbd)) {
                tsFound = t;
                break;
            }
        }

        if (tsFound != null) {
            if (boxTitleSearch != null) boxTitleSearch.setStyle("-fx-padding: 40 0 0 0;");
            hienThiKetQua(tsFound);
        } else {
            if (boxTitleSearch != null) boxTitleSearch.setStyle("-fx-padding: 150 0 0 0;");
            resultArea.setVisible(false);
            showAlert("Không tìm thấy", "Số báo danh không tồn tại trong hệ thống.");        }
    }
    private void themBongBongChat(String noiDung, boolean laNguoiDung) {
        Label lblChat = new Label(noiDung);
        lblChat.setWrapText(true);
        lblChat.setMaxWidth(260);
        if (laNguoiDung) {
            lblChat.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 15 15 0 15; -fx-font-size: 14px;");
        } else {
            lblChat.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 15 15 15 0; -fx-font-size: 14px;");
        }

        HBox chatRow = new HBox(lblChat);
        chatRow.setAlignment(laNguoiDung ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        // 4. Đẩy lên UI và cuộn xuống dòng cuối cùng
        Platform.runLater(() -> {
            boxChatHistory.getChildren().add(chatRow);
            scrollChat.layout();
            scrollChat.setVvalue(1.0); // Cuộn mượt mà xuống đáy
        });
    }

    @FXML
    protected void onSendChatClick() {
        String cauHoi = txtChatInput.getText().trim();
        if (cauHoi.isEmpty()) return;

        // In câu hỏi của người dùng (Bên phải - Màu xanh)
        themBongBongChat(cauHoi, true);
        txtChatInput.clear();

        // Thêm một bong bóng ảo báo hiệu Bot đang gõ
        themBongBongChat("🤖 Đang suy nghĩ...", false);

        new Thread(() -> {
            try {
                String traLoi = uniBot.hoiBot(cauHoi);

                Platform.runLater(() -> {
                    int lastIndex = boxChatHistory.getChildren().size() - 1;
                    if (lastIndex >= 0) boxChatHistory.getChildren().remove(lastIndex);

                    themBongBongChat(traLoi, false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    int lastIndex = boxChatHistory.getChildren().size() - 1;
                    if (lastIndex >= 0) boxChatHistory.getChildren().remove(lastIndex);
                    themBongBongChat("❌ Lỗi không lấy được dữ liệu", false);
                });
            }
        }).start();
    }

    private void hienThiKetQua(ThiSinh ts) {
        lblHoTen.setText(ts.getHoTen() != null ? ts.getHoTen().toUpperCase() : "CHƯA RÕ TÊN");
        lblNgaySinh.setText(ts.getNgaySinh() != null ? String.valueOf(ts.getNgaySinh()) : "??/??/????");
        lblQueQuan.setText(ts.getQueQuan() != null ? ts.getQueQuan() : "Chưa cập nhật");
        Diem d = ts.getDiemThi();
        if (d == null) return;

        StringBuilder sb = new StringBuilder();
        java.util.Map<String, Double> mapDiem = new java.util.LinkedHashMap<>();
        mapDiem.put("Toán", d.getToan()); mapDiem.put("Văn", d.getVan()); mapDiem.put("Anh", d.getAnh());
        mapDiem.put("Lý", d.getLy()); mapDiem.put("Hóa", d.getHoa()); mapDiem.put("Sinh", d.getSinh());
        mapDiem.put("Sử", d.getSu()); mapDiem.put("Địa", d.getDia()); mapDiem.put("GDKT", d.getGdkt());
        mapDiem.put("Tin", d.getTin()); mapDiem.put("C.Nghệ", d.getCongNghe());

        mapDiem.forEach((mon, diem) -> {
            if (diem != null && diem > 0) {
                if (sb.length() > 0) sb.append("  |  ");
                sb.append(mon).append(": ").append(diem);
            }
        });
        if (lblDiemChiTiet != null) lblDiemChiTiet.setText(sb.toString());

        boxKhoi.getChildren().clear();
        List<String> khoiHopLe = xacDinhKhoi(d);

        for (String k : khoiHopLe) {
            Button btn = new Button(k);
            btn.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-cursor: hand;");

            btn.setOnAction(e -> {
                boxKhoi.getChildren().forEach(n -> n.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-cursor: hand;"));
                btn.setStyle("-fx-background-color: #d35400; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

                double tong = tinhDiem(d, k);
                lblTongDiem.setText(String.format("%.2f", tong).replace(",", "."));

                if (lblBadge != null) lblBadge.setVisible(tong >= 27.0);
                hienThiGoiY(tong, k);
            });
            boxKhoi.getChildren().add(btn);
        }

        // Auto-click nút đầu tiên
        if (!boxKhoi.getChildren().isEmpty()) {
            ((Button) boxKhoi.getChildren().get(0)).fire();
        }

        resultArea.setVisible(true);
    }

    private List<String> xacDinhKhoi(Diem d) {
        List<String> list = new ArrayList<>();

        boolean toan = d.getToan() != null && d.getToan() > 0;
        boolean van  = d.getVan()  != null && d.getVan()  > 0;
        boolean anh  = d.getAnh()  != null && d.getAnh()  > 0;
        boolean ly   = d.getLy()   != null && d.getLy()   > 0;
        boolean hoa  = d.getHoa()  != null && d.getHoa()  > 0;
        boolean sinh = d.getSinh() != null && d.getSinh() > 0;
        boolean su   = d.getSu()   != null && d.getSu()   > 0;
        boolean dia  = d.getDia()  != null && d.getDia()  > 0;
        boolean gdkt = d.getGdkt() != null && d.getGdkt() > 0;
        boolean tin  = d.getTin()  != null && d.getTin()  > 0;

        if (toan && ly  && hoa ) list.add("A00");
        if (toan && ly  && anh ) list.add("A01");
        if (toan && ly  && sinh) list.add("A02");
        if (toan && ly  && su  ) list.add("A03");
        if (toan && ly  && dia ) list.add("A04");
        if (toan && hoa && su  ) list.add("A05");
        if (toan && hoa && dia ) list.add("A06");
        if (toan && su  && dia ) list.add("A07");
        if (toan && dia && gdkt) list.add("A09");
        if (toan && ly  && tin ) list.add("A10");
        if (toan && hoa && tin ) list.add("A11");

        if (toan && hoa && sinh) list.add("B00");
        if (toan && sinh && su ) list.add("B01");
        if (toan && sinh && dia) list.add("B02");
        if (toan && sinh && gdkt) list.add("B03");
        if (toan && hoa  && dia ) list.add("B04");
        if (toan && sinh && anh) list.add("B08");

        if (van  && su  && dia ) list.add("C00");
        if (toan && van && ly  ) list.add("C01");
        if (toan && van && hoa ) list.add("C02");
        if (toan && van && su  ) list.add("C03");
        if (toan && van && dia ) list.add("C04");
        if (van  && ly  && hoa ) list.add("C05");
        if (toan && su  && dia ) list.add("C06");
        if (toan && su  && gdkt) list.add("C07");
        if (toan && dia && gdkt) list.add("C08");
        if (van  && su  && tin ) list.add("C19");
        if (van  && su  && gdkt) list.add("C20");
        if (van  && dia && gdkt) list.add("C21");
        if (toan && van && gdkt) list.add("C22");

        if (toan && van  && anh ) list.add("D01");
        if (toan && hoa  && anh ) list.add("D07");
        if (toan && sinh && anh ) list.add("D08");
        if (toan && dia  && anh ) list.add("D09");
        if (toan && van  && tin ) list.add("D10");
        if (toan && su   && anh ) list.add("D11");
        if (toan && gdkt && anh ) list.add("D12");
        if (van  && hoa  && anh ) list.add("D13");
        if (van  && su   && anh ) list.add("D14");
        if (van  && dia  && anh ) list.add("D15");
        if (toan && anh  && tin ) list.add("D90");

        return list;
    }

    private double tinhDiem(Diem d, String k) {
        switch (k) {
            case "A00": return d.getToan() + d.getLy()   + d.getHoa();
            case "A01": return d.getToan() + d.getLy()   + d.getAnh();
            case "A02": return d.getToan() + d.getLy()   + d.getSinh();
            case "A03": return d.getToan() + d.getLy()   + d.getSu();
            case "A04": return d.getToan() + d.getLy()   + d.getDia();
            case "A05": return d.getToan() + d.getHoa()  + d.getSu();
            case "A06": return d.getToan() + d.getHoa()  + d.getDia();
            case "A07": return d.getToan() + d.getSu()   + d.getDia();
            case "A09": return d.getToan() + d.getDia()  + d.getGdkt();
            case "A10": return d.getToan() + d.getLy()   + d.getTin();
            case "A11": return d.getToan() + d.getHoa()  + d.getTin();

            case "B00": return d.getToan() + d.getHoa()  + d.getSinh();
            case "B01": return d.getToan() + d.getSinh() + d.getSu();
            case "B02": return d.getToan() + d.getSinh() + d.getDia();
            case "B03": return d.getToan() + d.getSinh() + d.getGdkt();
            case "B04": return d.getToan() + d.getHoa()  + d.getDia();
            case "B08": return d.getToan() + d.getSinh() + d.getAnh();

            case "C00": return d.getVan()  + d.getSu()   + d.getDia();
            case "C01": return d.getToan() + d.getVan()  + d.getLy();
            case "C02": return d.getToan() + d.getVan()  + d.getHoa();
            case "C03": return d.getToan() + d.getVan()  + d.getSu();
            case "C04": return d.getToan() + d.getVan()  + d.getDia();
            case "C05": return d.getVan()  + d.getLy()   + d.getHoa();
            case "C06": return d.getToan() + d.getSu()   + d.getDia();
            case "C07": return d.getToan() + d.getSu()   + d.getGdkt();
            case "C08": return d.getToan() + d.getDia()  + d.getGdkt();
            case "C19": return d.getVan()  + d.getSu()   + d.getTin();
            case "C20": return d.getVan()  + d.getSu()   + d.getGdkt();
            case "C21": return d.getVan()  + d.getDia()  + d.getGdkt();
            case "C22": return d.getToan() + d.getVan()  + d.getGdkt();

            case "D01": return d.getToan() + d.getVan()  + d.getAnh();
            case "D07": return d.getToan() + d.getHoa()  + d.getAnh();
            case "D08": return d.getToan() + d.getSinh() + d.getAnh();
            case "D09": return d.getToan() + d.getDia()  + d.getAnh();
            case "D10": return d.getToan() + d.getVan()  + d.getTin();
            case "D11": return d.getToan() + d.getSu()   + d.getAnh();
            case "D12": return d.getToan() + d.getGdkt() + d.getAnh();
            case "D13": return d.getVan()  + d.getHoa()  + d.getAnh();
            case "D14": return d.getVan()  + d.getSu()   + d.getAnh();
            case "D15": return d.getVan()  + d.getDia()  + d.getAnh();
            case "D90": return d.getToan() + d.getAnh()  + d.getTin();

            default: return 0.0;
        }
    }

    private void hienThiGoiY(double score, String k) {
        if (listTruongGoiY == null || suggestionBox == null || lblTitleGoiY == null) return;
        listTruongGoiY.getChildren().clear();
        suggestionBox.setVisible(score > 0);

        lblTitleGoiY.setText("🎯 Gợi ý trường phù hợp khối " + k + " (Điểm chuẩn ≤ " + score + "):");

        List<NganhHoc> nganhPhuHop = khoDuLieuNganh.stream()
                .filter(n -> n.khoi != null && n.khoi.contains(k) && n.diemChuan <= score && n.diemChuan > 0)
                .sorted((a, b) -> Double.compare(b.diemChuan, a.diemChuan)) // Ưu tiên xếp điểm cao lên trước
                .collect(Collectors.toList());

        List<String> truongGoiY = new ArrayList<>();
        for (NganhHoc n : nganhPhuHop) {
            if (n.tenTruong != null && !truongGoiY.contains(n.tenTruong)) {
                truongGoiY.add(n.tenTruong);
            }
            if (truongGoiY.size() == 3) break;
        }

        if (truongGoiY.isEmpty()) {
            listTruongGoiY.getChildren().add(new Label("😅 Chưa tìm thấy trường nào phù hợp trong hệ thống."));
        } else {
            for (String truong : truongGoiY) {
                listTruongGoiY.getChildren().add(new Label("🎓 " + truong));
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}