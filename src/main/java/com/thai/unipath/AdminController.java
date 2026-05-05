package com.thai.unipath;

import com.thai.unipath.StudentController.TruongDH;
import com.thai.unipath.StudentController.NganhHoc;
import com.thai.unipath.model.ThiSinh;
import com.thai.unipath.model.Diem;
import com.thai.unipath.service.ExcelServiceImpl;
import com.thai.unipath.service.IDataService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class AdminController {

    @FXML private TableView tableAdmin;
    @FXML private TextField fMa, fTen, fKhuVuc, fHe, fWeb;
    @FXML private Label lblHeaderTitle;
    @FXML private Button btnMenuTruong, btnMenuNganh, btnMenuThiSinh, btnMenuThongKe;
    @FXML private TitledPane paneEditForm;
    @FXML private Button btnSaveAll;

    // UI Panels để Swap
    @FXML private VBox viewData, viewThongKe;

    @FXML private TextField txtSearchAdmin;
    @FXML private PieChart pieChartThongKe;
    @FXML private BarChart<String, Number> barChartDiemMon;
    @FXML private ComboBox<String> cbMonThongKe;

    private final String FILE_TRUONG = "truong_dai_hoc.json";
    private final String FILE_NGANH = "nganh_hoc.json";
    private IDataService excelService = new ExcelServiceImpl();

    private ObservableList<TruongDH> listTruong = FXCollections.observableArrayList();
    private ObservableList<NganhHoc> listNganh = FXCollections.observableArrayList();
    private ObservableList<ThiSinh> listThiSinh = FXCollections.observableArrayList();

    class ScoreConverter extends StringConverter<Double> {
        @Override public Double fromString(String s) {
            if (s == null || s.trim().isEmpty()) return null;
            try { return Double.parseDouble(s); } catch (Exception e) { return null; }
        }
        @Override public String toString(Double d) { return d == null ? "" : String.valueOf(d); }
    }

    @FXML
    public void initialize() {
        tableAdmin.setEditable(true);

        // Nạp danh sách môn học vào ComboBox Thống Kê
        if (cbMonThongKe != null) {
            cbMonThongKe.getItems().addAll("Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh", "Sử", "Địa", "Tin", "GDKT", "C.Nghệ");
            cbMonThongKe.getSelectionModel().selectFirst();
            cbMonThongKe.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) vePhoDiemMon(newVal);
            });
        }

        List<ThiSinh> list = excelService.readData("DuLieu.xlsx");
        listThiSinh.setAll(list);
        btnMenuThiSinh.fire();
    }

    @FXML
    protected void onMenuClick(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String styleOff = "-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-alignment: CENTER_LEFT; -fx-font-weight: bold; -fx-cursor: hand;";
        btnMenuTruong.setStyle(styleOff); btnMenuNganh.setStyle(styleOff);
        btnMenuThiSinh.setStyle(styleOff); btnMenuThongKe.setStyle(styleOff);
        btn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-font-weight: bold; -fx-cursor: hand;");

        String text = btn.getText();
        clearForm();
        if(txtSearchAdmin != null) txtSearchAdmin.clear();

        // Xử lý chuyển đổi View
        if (text.contains("Thống kê")) {
            viewData.setVisible(false); viewData.setManaged(false);
            viewThongKe.setVisible(true); viewThongKe.setManaged(true);
            btnSaveAll.setVisible(false); // Tab thống kê thì giấu nút Lưu đi
            lblHeaderTitle.setText("📊 Bảng Điều Khiển Thống Kê & Báo Cáo");
            loadBieuDoThongKe();
            return;
        } else {
            viewData.setVisible(true); viewData.setManaged(true);
            viewThongKe.setVisible(false); viewThongKe.setManaged(false);
            btnSaveAll.setVisible(true);
        }

        if (text.contains("Trường")) {
            paneEditForm.setText("Cập nhật thông tin (Dành cho Trường Đại học)");
            fMa.setPromptText("Mã Trường (VD: PKA)"); fTen.setPromptText("Tên Trường");
            fKhuVuc.setPromptText("Khu vực"); fHe.setPromptText("Hệ đào tạo");
            fWeb.setPromptText("Website");
            fHe.setDisable(false); fWeb.setDisable(false);
            showQuanLyTruong();
        }
        else if (text.contains("Ngành")) {
            paneEditForm.setText("Thêm Ngành Mới (Nhập cơ bản rồi sửa chi tiết trên bảng)");
            fMa.setPromptText("Mã Xét Tuyển (VD: IT1)"); fTen.setPromptText("Tên Ngành");
            fKhuVuc.setPromptText("Nhóm Ngành"); fHe.setPromptText("Tên Trường Đại Học");
            fWeb.setPromptText("Khối thi (VD: A00)");
            fHe.setDisable(false); fWeb.setDisable(false);
            showQuanLyNganh();
        }
        else if (text.contains("Thí sinh")) {
            paneEditForm.setText("Thêm Thí Sinh Mới (Tạo hồ sơ trước, nhập điểm trực tiếp trên bảng)");
            fMa.setPromptText("Số Báo Danh"); fTen.setPromptText("Họ và Tên");
            fKhuVuc.setPromptText("Ngày sinh (YYYY-MM-DD)");
            fHe.setPromptText("Quê Quán"); fWeb.setPromptText("<< Không dùng >>");
            fHe.setDisable(false); fWeb.setDisable(true);
            showQuanLyThiSinh();
        }
    }

    private void clearForm() { fMa.clear(); fTen.clear(); fKhuVuc.clear(); fHe.clear(); fWeb.clear(); }

    private void showQuanLyTruong() {
        lblHeaderTitle.setText("🏫 Quản lý Danh sách Trường");
        tableAdmin.getColumns().clear();
        addSTTColumn();
        addEditableStrCol("Mã", t -> ((TruongDH)t).ma, (t, v) -> ((TruongDH)t).ma = v, 100);
        addEditableStrCol("Tên Trường", t -> ((TruongDH)t).ten, (t, v) -> ((TruongDH)t).ten = v, 300);
        addEditableStrCol("Khu Vực", t -> ((TruongDH)t).khuVuc, (t, v) -> ((TruongDH)t).khuVuc = v, 150);
        addEditableStrCol("Hệ ĐT", t -> ((TruongDH)t).heDaoTao, (t, v) -> ((TruongDH)t).heDaoTao = v, 100);
        addEditableStrCol("Website", t -> ((TruongDH)t).web, (t, v) -> ((TruongDH)t).web = v, 200);

        try (FileReader r = new FileReader(FILE_TRUONG)) {
            List<TruongDH> data = new Gson().fromJson(r, new TypeToken<ArrayList<TruongDH>>(){}.getType());
            if (data != null) listTruong.setAll(data);

            FilteredList<TruongDH> filteredTruong = new FilteredList<>(listTruong, b -> true);
            txtSearchAdmin.textProperty().addListener((obs, oldV, newV) -> {
                filteredTruong.setPredicate(t -> {
                    if (newV == null || newV.isEmpty()) return true;
                    String filter = newV.toLowerCase();
                    return (t.ten != null && t.ten.toLowerCase().contains(filter)) ||
                            (t.ma != null && t.ma.toLowerCase().contains(filter));
                });
            });
            tableAdmin.setItems(filteredTruong);
        } catch (Exception e) {}
    }

    private void showQuanLyNganh() {
        lblHeaderTitle.setText("💼 Quản lý Ngành & Điểm chuẩn");
        tableAdmin.getColumns().clear();

        addSTTColumn();
        addEditableStrCol("Mã Ngành", t -> ((NganhHoc)t).maNganh, (t, v) -> ((NganhHoc)t).maNganh = v, 100);
        addEditableStrCol("Tên Ngành", t -> ((NganhHoc)t).tenNganh, (t, v) -> ((NganhHoc)t).tenNganh = v, 220);
        addEditableStrCol("Nhóm Ngành", t -> ((NganhHoc)t).nhomNganh, (t, v) -> ((NganhHoc)t).nhomNganh = v, 180);
        addEditableStrCol("Trường Đào Tạo", t -> ((NganhHoc)t).tenTruong, (t, v) -> ((NganhHoc)t).tenTruong = v, 250);
        addEditableStrCol("Khối", t -> ((NganhHoc)t).khoi, (t, v) -> ((NganhHoc)t).khoi = v, 120);
        addEditableDoubleCol("Điểm Chuẩn", t -> ((NganhHoc)t).diemChuan, (t, v) -> ((NganhHoc)t).diemChuan = (v != null ? v : 0.0), 100);

        try (FileReader r = new FileReader(FILE_NGANH)) {
            List<NganhHoc> data = new Gson().fromJson(r, new TypeToken<ArrayList<NganhHoc>>(){}.getType());
            if (data != null) {
                data.sort(Comparator.comparing(n -> n.nhomNganh != null ? n.nhomNganh : ""));
                listNganh.setAll(data);
            }

            FilteredList<NganhHoc> filteredNganh = new FilteredList<>(listNganh, b -> true);
            txtSearchAdmin.textProperty().addListener((obs, oldV, newV) -> {
                filteredNganh.setPredicate(n -> {
                    if (newV == null || newV.isEmpty()) return true;
                    String filter = newV.toLowerCase();
                    return (n.tenNganh != null && n.tenNganh.toLowerCase().contains(filter)) ||
                            (n.maNganh != null && n.maNganh.toLowerCase().contains(filter)) ||
                            (n.tenTruong != null && n.tenTruong.toLowerCase().contains(filter));
                });
            });
            tableAdmin.setItems(filteredNganh);
        } catch (Exception e) {}
    }

    private void showQuanLyThiSinh() {
        lblHeaderTitle.setText("👤 Quản lý Hồ sơ & Sửa Điểm Thí sinh");
        tableAdmin.getColumns().clear();
        tableAdmin.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        addSTTColumn();
        addEditableStrCol("SBD", t -> ((ThiSinh)t).getSbd(), (t, v) -> ((ThiSinh)t).setSbd(v), 80);
        addEditableStrCol("Họ Tên", t -> ((ThiSinh)t).getHoTen(), (t, v) -> ((ThiSinh)t).setHoTen(v), 150);
        addEditableStrCol("Ngày Sinh", t -> ((ThiSinh)t).getNgaySinh() != null ? ((ThiSinh)t).getNgaySinh().toString() : "",
                (t, v) -> { try { ((ThiSinh)t).setNgaySinh(LocalDate.parse(v)); } catch(Exception e) {} }, 100);
        addEditableStrCol("Quê Quán", t -> ((ThiSinh)t).getQueQuan(), (t, v) -> ((ThiSinh)t).setQueQuan(v), 120);

        addEditableDoubleCol("Toán", t -> getScore((ThiSinh)t, Diem::getToan), (t, v) -> setScore((ThiSinh)t, Diem::setToan, v), 60);
        addEditableDoubleCol("Văn", t -> getScore((ThiSinh)t, Diem::getVan), (t, v) -> setScore((ThiSinh)t, Diem::setVan, v), 60);
        addEditableDoubleCol("Anh", t -> getScore((ThiSinh)t, Diem::getAnh), (t, v) -> setScore((ThiSinh)t, Diem::setAnh, v), 60);
        addEditableDoubleCol("Lý", t -> getScore((ThiSinh)t, Diem::getLy), (t, v) -> setScore((ThiSinh)t, Diem::setLy, v), 60);
        addEditableDoubleCol("Hóa", t -> getScore((ThiSinh)t, Diem::getHoa), (t, v) -> setScore((ThiSinh)t, Diem::setHoa, v), 60);
        addEditableDoubleCol("Sinh", t -> getScore((ThiSinh)t, Diem::getSinh), (t, v) -> setScore((ThiSinh)t, Diem::setSinh, v), 60);
        addEditableDoubleCol("Sử", t -> getScore((ThiSinh)t, Diem::getSu), (t, v) -> setScore((ThiSinh)t, Diem::setSu, v), 60);
        addEditableDoubleCol("Địa", t -> getScore((ThiSinh)t, Diem::getDia), (t, v) -> setScore((ThiSinh)t, Diem::setDia, v), 60);
        addEditableDoubleCol("GDKT", t -> getScore((ThiSinh)t, Diem::getGdkt), (t, v) -> setScore((ThiSinh)t, Diem::setGdkt, v), 60);
        addEditableDoubleCol("Tin", t -> getScore((ThiSinh)t, Diem::getTin), (t, v) -> setScore((ThiSinh)t, Diem::setTin, v), 60);
        addEditableDoubleCol("C.Nghệ", t -> getScore((ThiSinh)t, Diem::getCongNghe), (t, v) -> setScore((ThiSinh)t, Diem::setCongNghe, v), 60);

        FilteredList<ThiSinh> filteredThiSinh = new FilteredList<>(listThiSinh, b -> true);
        txtSearchAdmin.textProperty().addListener((obs, oldV, newV) -> {
            filteredThiSinh.setPredicate(ts -> {
                if (newV == null || newV.isEmpty()) return true;
                String filter = newV.toLowerCase();
                if (ts.getHoTen() != null && ts.getHoTen().toLowerCase().contains(filter)) return true;
                if (ts.getSbd() != null && ts.getSbd().toLowerCase().contains(filter)) return true;
                if (ts.getQueQuan() != null && ts.getQueQuan().toLowerCase().contains(filter)) return true;
                return false;
            });
        });
        tableAdmin.setItems(filteredThiSinh);
    }

    private void loadBieuDoThongKe() {
        if (pieChartThongKe == null) return;
        int mucXuatSac = 0, mucKhaGioi = 0, mucTrungBinh = 0;

        for (ThiSinh ts : listThiSinh) {
            Diem d = ts.getDiemThi();
            if (d == null) continue;

            double tongDiem = 0;
            if (d.getToan() != null && d.getToan() > 0) tongDiem += d.getToan();
            if (d.getVan() != null && d.getVan() > 0)   tongDiem += d.getVan();
            if (d.getAnh() != null && d.getAnh() > 0)   tongDiem += d.getAnh();
            if (d.getLy() != null && d.getLy() > 0)     tongDiem += d.getLy();
            if (d.getHoa() != null && d.getHoa() > 0)   tongDiem += d.getHoa();
            if (d.getSinh() != null && d.getSinh() > 0) tongDiem += d.getSinh();
            if (d.getSu() != null && d.getSu() > 0)     tongDiem += d.getSu();
            if (d.getDia() != null && d.getDia() > 0)   tongDiem += d.getDia();
            if (d.getTin() != null && d.getTin() > 0)   tongDiem += d.getTin();
            if (d.getGdkt() != null && d.getGdkt() > 0) tongDiem += d.getGdkt();
            if (d.getCongNghe() != null && d.getCongNghe() > 0) tongDiem += d.getCongNghe();

            if (tongDiem >= 32) mucXuatSac++;
            else if (tongDiem >= 20) mucKhaGioi++;
            else if (tongDiem > 0) mucTrungBinh++;
        }

        pieChartThongKe.setData(FXCollections.observableArrayList(
                new PieChart.Data("Xuất sắc (>= 32đ) - " + mucXuatSac, mucXuatSac),
                new PieChart.Data("Khá Giỏi (20-31đ) - " + mucKhaGioi, mucKhaGioi),
                new PieChart.Data("Trung bình (< 20đ) - " + mucTrungBinh, mucTrungBinh)
        ));

        if (cbMonThongKe != null && cbMonThongKe.getValue() != null) {
            vePhoDiemMon(cbMonThongKe.getValue());
        }
    }

    private void vePhoDiemMon(String mon) {
        if (barChartDiemMon == null) return;
        barChartDiemMon.getData().clear();

        int dKem = 0, dYeu = 0, dTb = 0, dKha = 0, dGioi = 0, dXuatSac = 0;

        for (ThiSinh ts : listThiSinh) {
            Diem d = ts.getDiemThi();
            if (d == null) continue;

            Double score = null;
            switch(mon) {
                case "Toán": score = d.getToan(); break;
                case "Văn": score = d.getVan(); break;
                case "Anh": score = d.getAnh(); break;
                case "Lý": score = d.getLy(); break;
                case "Hóa": score = d.getHoa(); break;
                case "Sinh": score = d.getSinh(); break;
                case "Sử": score = d.getSu(); break;
                case "Địa": score = d.getDia(); break;
                case "Tin": score = d.getTin(); break;
                case "GDKT": score = d.getGdkt(); break;
                case "C.Nghệ": score = d.getCongNghe(); break;
            }

            if (score != null && score >= 0) {
                if (score < 4.0) dKem++;
                else if (score < 5.0) dYeu++;
                else if (score < 7.0) dTb++;
                else if (score < 8.0) dKha++;
                else if (score < 9.0) dGioi++;
                else dXuatSac++;
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Phổ điểm môn: " + mon);

        series.getData().add(new XYChart.Data<>("< 4 (Kém)", dKem));
        series.getData().add(new XYChart.Data<>("4 - 5 (Yếu)", dYeu));
        series.getData().add(new XYChart.Data<>("5 - 7 (TB)", dTb));
        series.getData().add(new XYChart.Data<>("7 - 8 (Khá)", dKha));
        series.getData().add(new XYChart.Data<>("8 - 9 (Giỏi)", dGioi));
        series.getData().add(new XYChart.Data<>("9 - 10 (Xuất Sắc)", dXuatSac));

        barChartDiemMon.getData().add(series);
    }

    private void addSTTColumn() {
        TableColumn<Object, String> colSTT = new TableColumn<>("STT"); colSTT.setPrefWidth(40);
        colSTT.setCellFactory(col -> new TableCell<Object, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        tableAdmin.getColumns().add(colSTT);
    }

    private <T> void addEditableStrCol(String title, Function<T, String> getter, BiConsumer<T, String> setter, double w) {
        TableColumn<T, String> col = new TableColumn<>(title); col.setPrefWidth(w);
        col.setCellValueFactory(cell -> new SimpleStringProperty(getter.apply(cell.getValue())));
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setOnEditCommit(event -> setter.accept(event.getRowValue(), event.getNewValue()));
        tableAdmin.getColumns().add(col);
    }

    private <T> void addEditableDoubleCol(String title, Function<T, Double> getter, BiConsumer<T, Double> setter, double w) {
        TableColumn<T, Double> col = new TableColumn<>(title); col.setPrefWidth(w);
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(getter.apply(cell.getValue())));
        col.setCellFactory(TextFieldTableCell.forTableColumn(new ScoreConverter()));
        col.setOnEditCommit(event -> setter.accept(event.getRowValue(), event.getNewValue()));
        tableAdmin.getColumns().add(col);
    }

    private Double getScore(ThiSinh ts, Function<Diem, Double> getter) { return ts.getDiemThi() != null ? getter.apply(ts.getDiemThi()) : null; }
    private void setScore(ThiSinh ts, BiConsumer<Diem, Double> setter, Double val) {
        if (ts.getDiemThi() == null) ts.setDiemThi(new Diem());
        setter.accept(ts.getDiemThi(), val);
    }

    @FXML
    protected void onAdd() {
        String title = lblHeaderTitle.getText();

        if (title.contains("Trường")) {
            TruongDH t = new TruongDH();
            t.ma = fMa.getText();
            t.ten = fTen.getText();
            t.khuVuc = fKhuVuc.getText();
            t.heDaoTao = fHe.getText();
            t.web = fWeb.getText();
            listTruong.add(0, t);
        }
        else if (title.contains("Ngành")) {
            NganhHoc n = new NganhHoc();
            n.maNganh = fMa.getText();
            n.tenNganh = fTen.getText();
            n.nhomNganh = fKhuVuc.getText();
            n.tenTruong = fHe.getText();
            n.khoi = fWeb.getText();
            n.diemChuan = 0.0;
            n.hocPhi = 0.0;
            listNganh.add(0, n);
        }
        else if (title.contains("Thí sinh")) {
            ThiSinh ts = new ThiSinh();

            String sbdInput = fMa.getText().trim();
            String hoTenInput = fTen.getText().trim();
            String queQuanInput = fHe.getText().trim();

            ts.setSbd(sbdInput.isEmpty() ? "SBD_TEMP" : sbdInput);
            ts.setHoTen(hoTenInput.isEmpty() ? "Chưa nhập tên" : hoTenInput);
            ts.setQueQuan(queQuanInput.isEmpty() ? "Chưa nhập quê quán" : queQuanInput);

            try {
                if (!fKhuVuc.getText().isEmpty()) {
                    ts.setNgaySinh(java.time.LocalDate.parse(fKhuVuc.getText().trim()));
                }
            } catch (Exception e) {
                System.out.println("Lỗi ngày sinh!");
            }

            ts.setDiemThi(new Diem());
            listThiSinh.add(0, ts);
            loadBieuDoThongKe();
        }

        clearForm();
        tableAdmin.scrollTo(0);
    }

    @FXML protected void onDelete() {
        Object selected = tableAdmin.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String title = lblHeaderTitle.getText();
            if (title.contains("Trường")) listTruong.remove(selected);
            else if (title.contains("Ngành")) listNganh.remove(selected);
            else if (title.contains("Thí sinh")) {
                listThiSinh.remove(selected);
                loadBieuDoThongKe();
            }
        }
    }

    @FXML
    protected void onSave() {
        String title = lblHeaderTitle.getText();

        if (title.contains("Thí sinh")) {
            try {
                List<ThiSinh> dataToSave = new ArrayList<>(listThiSinh);
                excelService.writeData(dataToSave, "DuLieu.xlsx");

                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Thành công");
                a.setHeaderText(null);
                a.setContentText("Đã lưu danh sách thí sinh vào file DuLieu.xlsx!");
                a.show();
            } catch (Exception e) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Lỗi");
                a.setContentText("Không thể lưu file Excel. Kiểm tra xem file có đang mở không nhé!");
                a.show();
            }
        }
        else {
            String fileName = title.contains("Trường") ? FILE_TRUONG : FILE_NGANH;
            List dataToSave = title.contains("Trường") ? new ArrayList<>(listTruong) : new ArrayList<>(listNganh);

            try (Writer w = new FileWriter(fileName)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(dataToSave, w);
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Thành công");
                a.setHeaderText(null);
                a.setContentText("Đã lưu dữ liệu vào " + fileName);
                a.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML protected void onLogout() { tableAdmin.getScene().getWindow().hide(); }
}