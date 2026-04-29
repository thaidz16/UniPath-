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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
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
    @FXML private Button btnMenuTruong, btnMenuNganh, btnMenuThiSinh;
    @FXML private TitledPane paneEditForm;

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
        btnMenuNganh.fire(); // Ưu tiên load tab Ngành lên trước để test bản đại phẫu
    }

    @FXML
    protected void onMenuClick(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String styleOff = "-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-alignment: CENTER_LEFT; -fx-font-weight: bold; -fx-cursor: hand;";
        btnMenuTruong.setStyle(styleOff); btnMenuNganh.setStyle(styleOff); btnMenuThiSinh.setStyle(styleOff);
        btn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-font-weight: bold; -fx-cursor: hand;");

        String text = btn.getText();
        clearForm();

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
            fHe.setPromptText("Quê Quán "); fWeb.setPromptText("<< Không dùng >>");
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
            tableAdmin.setItems(listTruong);
        } catch (Exception e) {}
    }

    // --- SAU ĐẠI PHẪU: BẢNG NGÀNH GIỜ CÓ ĐỦ MÃ, TÊN TRƯỜNG, HỌC PHÍ ---
    private void showQuanLyNganh() {
        lblHeaderTitle.setText("💼 Quản lý Ngành & Điểm chuẩn (Đại phẫu thuật)");
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
                // Gom chung các Ngành giống nhau vào một cụm
                data.sort(Comparator.comparing(n -> n.nhomNganh != null ? n.nhomNganh : ""));
                listNganh.setAll(data);
            }
            tableAdmin.setItems(listNganh);
        } catch (Exception e) {}
    }

    private void showQuanLyThiSinh() {
        lblHeaderTitle.setText("👤 Quản lý Hồ sơ & Sửa Điểm Thí sinh (Excel)");
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

        List<ThiSinh> list = excelService.readData("DuLieu.xlsx");
        listThiSinh.setAll(list);
        tableAdmin.setItems(listThiSinh);
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
        }

        clearForm();
        tableAdmin.scrollTo(0);
    }

    @FXML protected void onDelete() {
        Object selected = tableAdmin.getSelectionModel().getSelectedItem();
        if (selected != null) tableAdmin.getItems().remove(selected);
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