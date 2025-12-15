public class Main {


    static class User implements Serializable {
        private static final long serialVersionUID = 1L;

        String name;
        int age;
        String email;

        User(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        void normalize() {
            name = name.trim();
            email = email.trim().toLowerCase();
        }

        @Override
        public String toString() {
            return name + "," + age + "," + email;
        }
    }


    static class Validator {
        static void validate(User u) {
            if (u.name == null || u.name.isBlank())
                throw new IllegalArgumentException("Name is empty");

            if (u.age < 0 || u.age > 120)
                throw new IllegalArgumentException("Invalid age: " + u.age);

            if (u.email == null || !u.email.contains("@"))
                throw new IllegalArgumentException("Invalid email: " + u.email);
        }
    }

    static class CsvUtil {

        static List<User> importCsv(String csv) {
            List<User> users = new ArrayList<>();
            String[] lines = csv.split("\n");

            for (String line : lines) {
                try {
                    String[] parts = line.split(",");
                    if (parts.length != 3)
                        throw new IllegalArgumentException("Wrong column count");

                    User u = new User(
                            parts[0],
                            Integer.parseInt(parts[1]),
                            parts[2]
                    );

                    Validator.validate(u);
                    users.add(u);

                } catch (Exception e) {
                    System.out.println("⚠ Пропущено рядок CSV: " + line + " | причина: " + e.getMessage());
                }
            }
            return users;
        }


        static String exportCsv(List<User> users) {
            StringBuilder sb = new StringBuilder();
            for (User u : users) {
                sb.append(u).append("\n");
            }
            return sb.toString();
        }
    }


    static class SerializationUtil {

        static byte[] serialize(List<User> users) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(users);
            }
            return bos.toByteArray();
        }

        @SuppressWarnings("unchecked")
        static List<User> deserialize(byte[] data)
                throws IOException, ClassNotFoundException {

            try (ObjectInputStream ois =
                         new ObjectInputStream(new ByteArrayInputStream(data))) {
                return (List<User>) ois.readObject();
            }
        }
    }


    public static void main(String[] args) throws Exception {

        String csvInput = """
                Alice,30,ALICE@MAIL.COM
                Bob,200,bob@mail.com
                ,25,no_name@mail.com
                Charlie,40,charlie@mail.com
                Dave,22,davemail.com
                """;

        System.out.println("=== CSV IMPORT ===");
        List<User> users = CsvUtil.importCsv(csvInput);


        users.forEach(User::normalize);

        System.out.println("\n Валидные пользователи:");
        users.forEach(System.out::println);


        String csvOutput = CsvUtil.exportCsv(users);
        System.out.println("\n=== CSV EXPORT ===");
        System.out.println(csvOutput);

        System.out.println("=== SERIALIZATION COMPARISON ===");

        long t1 = System.nanoTime();
        byte[] binary = SerializationUtil.serialize(users);
        long t2 = System.nanoTime();

        long t3 = System.nanoTime();
        byte[] text = csvOutput.getBytes(StandardCharsets.UTF_8);
        long t4 = System.nanoTime();


        List<User> restored =
                SerializationUtil.deserialize(binary);

        System.out.println("Binary size: " + binary.length + " bytes");
        System.out.println("Text size:   " + text.length + " bytes");

        System.out.println("Binary serialize time: " + (t2 - t1) + " ns");
        System.out.println("Text convert time:     " + (t4 - t3) + " ns");

        System.out.println("\n=== DESERIALIZED OBJECTS ===");
        restored.forEach(System.out::println);

 
        System.out.println("\n=== ВИСНОВКИ ===");
        System.out.println;
    }
}
