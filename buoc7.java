        }

        String taskId = UUID.randomUUID().toString(); // YAGNI: Có thể dùng số nguyên tăng dần đơn giản hơn.

        JSONObject newTask = new JSONObject();
        newTask.put("id", taskId);
        newTask.put("title", title);
