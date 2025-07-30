        newTask.put("is_recurring", isRecurring); // YAGNI: Thêm thuộc tính này dù chưa có chức năng xử lý nhiệm vụ lặp lại
        if (isRecurring) {

            newTask.put("recurrence_pattern", "Chưa xác định");
        }
