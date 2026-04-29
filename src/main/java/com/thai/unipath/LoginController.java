package com.thai.unipath;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Button btnLogin;

    @FXML
    protected void onLoginClick() {
        String user = txtUser.getText();
        String pass = txtPass.getText();

        // Tài khoản admin của ông đây
        if (user.equals("admin") && pass.equals("thai123")) {
            try {
                // 1. Tắt cái cửa sổ Login hiện tại đi cho đỡ vướng
                Stage loginStage = (Stage) btnLogin.getScene().getWindow();
                loginStage.close();

                // 2. Triệu hồi màn hình Admin Dashboard lên
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("admin-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                Stage adminStage = new Stage();

                adminStage.setTitle("Hệ thống Quản trị UniPath - Admin: " + user);
                adminStage.setScene(scene);

                // Cho nó hiện ra giữa màn hình cho đẹp
                adminStage.show();

                System.out.println("Đã mở cửa phòng Admin thành công! 😎");

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi rồi sếp ơi: Không tìm thấy file admin-view.fxml!");
                alert.show();
            }
        } else {
            // Sai thì báo lỗi chứ không tắt cửa sổ
            Alert alert = new Alert(Alert.AlertType.ERROR, "Sai tài khoản hoặc mật khẩu rồi ông giáo ạ!");
            alert.show();
        }
    }

    @FXML
    protected void onBackClick() {
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        stage.close(); // Đóng cửa sổ login quay về màn chính
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}