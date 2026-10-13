# Build APK bằng GitHub Actions

1. Giải nén ZIP này.
2. Upload toàn bộ nội dung vào một GitHub repository.
3. Commit vào branch `main` hoặc `master`.
4. Vào tab **Actions**.
5. Chọn workflow **Build APK**.
6. Bấm **Run workflow** nếu workflow chưa tự chạy.
7. Khi chạy xong, mở lần chạy thành công.
8. Kéo xuống **Artifacts** và tải `WibuBankQR-debug`.
9. Giải nén artifact để lấy `app-debug.apk`.

Không upload file ZIP vào repo rồi chờ GitHub tự build; hãy upload các thư mục/file bên trong ZIP.
