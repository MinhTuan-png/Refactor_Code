import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PersonalTaskManagerViolations {

    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ✅ Load tasks từ DB
    private static JSONArray loadTasksFromDb() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            }
        } catch (IOException | ParseException e) {
            System.err.println("Lỗi khi đọc file database: " + e.getMessage());
        }
        return new JSONArray();
    }

    // ✅ Lưu tasks vào DB
    private static void saveTasksToDb(JSONArray tasksData) {
        try (FileWriter file = new FileWriter(DB_FILE_PATH)) {
            file.write(tasksData.toJSONString());
            file.flush();
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi vào file database: " + e.getMessage());
        }
    }

    // ✅ Kiểm tra tiêu đề
    private boolean isValidTitle(String title) {
        return title != null && !title.trim().isEmpty();
    }

    // ✅ Parse ngày đến hạn
    private LocalDate parseDueDate(String dueDateStr) {
        try {
            return LocalDate.parse(dueDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ✅ Kiểm tra mức độ ưu tiên
    private boolean isValidPriority(String priority) {
        return "Thấp".equals(priority) || "Trung bình".equals(priority) || "Cao".equals(priority);
    }

    /**
     * ✅ Thêm nhiệm vụ mới (đã resolve conflict)
     */
    public JSONObject addNewTaskWithViolations(String title, String description,
                                               String dueDateStr, String priorityLevel,
                                               boolean isRecurring) {

        // 🔹 Kiểm tra tiêu đề
        if (!isValidTitle(title)) {
            System.out.println("Lỗi: Tiêu đề không được để trống.");
            return null;
        }

        // 🔹 Kiểm tra ngày đến hạn
        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            System.out.println("Lỗi: Ngày đến hạn không được để trống.");
            return null;
        }
        LocalDate dueDate = parseDueDate(dueDateStr);
        if (dueDate == null) {
            System.out.println("Lỗi: Ngày đến hạn không hợp lệ. Vui lòng sử dụng định dạng YYYY-MM-DD.");
            return null;
        }

        // 🔹 Kiểm tra mức độ ưu tiên
        if (!isValidPriority(priorityLevel)) {
            System.out.println("Lỗi: Mức độ ưu tiên không hợp lệ. Vui lòng chọn từ: Thấp, Trung bình, Cao.");
            return null;
        }

        // 🔹 Load dữ liệu từ DB
        JSONArray tasks = loadTasksFromDb();

        // 🔹 Kiểm tra trùng lặp (title + due_date giống nhau)
        for (Object obj : tasks) {
            JSONObject existingTask = (JSONObject) obj;
            if (existingTask.get("title").toString().equalsIgnoreCase(title) &&
                existingTask.get("due_date").toString().equals(dueDate.format(DATE_FORMATTER))) {
                System.out.printf("Lỗi: Nhiệm vụ '%s' đã tồn tại với cùng ngày đến hạn.%n", title);
                return null;
            }
        }

        // 🔹 Tạo ID bằng UUID
        String taskId = UUID.randomUUID().toString();

        // 🔹 Tạo task mới
        JSONObject newTask = new JSONObject();
        newTask.put("id", taskId);
        newTask.put("title", title);
        newTask.put("description", description);
        newTask.put("due_date", dueDate.format(DATE_FORMATTER));
        newTask.put("priority", priorityLevel);
        newTask.put("status", "Chưa hoàn thành");
        newTask.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        newTask.put("last_updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        newTask.put("is_recurring", isRecurring);
        if (isRecurring) {
            newTask.put("recurrence_pattern", "Chưa xác định");
        }

        // 🔹 Lưu vào DB
        tasks.add(newTask);
        saveTasksToDb(tasks);

        System.out.printf("✅ Đã thêm nhiệm vụ mới thành công với ID: %s%n", taskId);
        return newTask;
    }

    // ✅ Demo test
    public static void main(String[] args) {
        PersonalTaskManagerViolations manager = new PersonalTaskManagerViolations();

        System.out.println("\n▶ Thêm nhiệm vụ hợp lệ:");
        manager.addNewTaskWithViolations("Mua sách", "Sách Công nghệ phần mềm.", "2025-07-20", "Cao", false);

        System.out.println("\n▶ Thêm nhiệm vụ trùng lặp:");
        manager.addNewTaskWithViolations("Mua sách", "Sách Công nghệ phần mềm.", "2025-07-20", "Cao", false);

        System.out.println("\n▶ Thêm nhiệm vụ lặp lại:");
        manager.addNewTaskWithViolations("Tập thể dục", "Tập gym 1 tiếng.", "2025-07-21", "Trung bình", true);

        System.out.println("\n▶ Thêm nhiệm vụ với tiêu đề rỗng:");
        manager.addNewTaskWithViolations("", "Nhiệm vụ không có tiêu đề.", "2025-07-22", "Thấp", false);
    }
}
