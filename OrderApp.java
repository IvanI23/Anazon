import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

class Inventory {                                           // Class to manage inventory
    public HashMap<String, Integer[]> inventoryDisplay() {
        HashMap<String, Integer[]> inventory = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("inventory.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue; // Skip malformed lines
                String name = parts[0];
                Integer[] values = {Integer.parseInt(parts[1]), Integer.parseInt(parts[2])}; // Price, Stock
                inventory.put(name, values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inventory;
    }

    public void updateInventory(HashMap<String, Integer[]> inventory) {
        try (FileWriter writer = new FileWriter("inventory.csv")) {
            for (String key : inventory.keySet()) {
                Integer[] values = inventory.get(key);
                writer.write(key + "," + values[0] + "," + values[1] + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Basket {                                        // Class to manage basket
    HashMap<String, Integer[]> basket = new HashMap<>();
    String id;
    
    public Basket(String id) {
        this.id = id;
        this.basket = contents(id);
    }

    private HashMap<String, Integer[]> contents(String id){
        HashMap<String, Integer[]> data = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(","); 
                
                if (parts.length < 3) continue; 
                
                if (id.equals(parts[0])) {
                    try {
                        String dictionary = parts[2].substring(1, parts[2].length() - 1); 
                        
                        String[] pairs = dictionary.split("\\|"); 
                        
                        for (String pair : pairs) {
                            String[] values = pair.split("="); 
                            if (values.length != 2) continue; 
                            
                            String key = values[0].trim();
                            String[] valueParts = values[1].split(":");

                            if (valueParts.length != 2) continue;
                            
                            Integer firstValue = Integer.parseInt(valueParts[0].trim());
                            Integer secondValue = Integer.parseInt(valueParts[1].trim());
                            data.put(key, new Integer[]{firstValue, secondValue});
                        }
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
        
        return data;
    }


    public void addToBasket(String name, Integer[] values, String user) {
        if (basket.containsKey(name)) {
            Integer[] currentValues = basket.get(name);
            currentValues[1] += 1; 
        } else {
            basket.put(name, new Integer[]{values[0], 1}); 
        }

        ArrayList <String> content = new ArrayList<>();
        try (BufferedReader read = new BufferedReader(new FileReader("users.csv"))){
            String line;

            while ((line = read.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(user)) {
                    String toAdd;
                    String username = parts[0];
                    String password = parts[1];
                    String total = parts[3];
                    String bought = parts[4];
                    toAdd = (username + "," + password + "," + "{");
                    for (String key : basket.keySet()) {
                        Integer[] values2 = basket.get(key);
                        toAdd += key + "=" + values2[0] + ":" + values2[1] + "|";
                    }

                    toAdd = toAdd.substring(0, toAdd.length() - 1);
                    toAdd += "}";
                    toAdd += "," + total + "," + bought;
                    content.add(toAdd);
                } else{
                    content.add(line);
                }
                }
            }
        catch (IOException e) {
            e.printStackTrace();
        }

        try (FileWriter writer = new FileWriter("users.csv")) {
            writer.write("");
            for (String line : content) {
                writer.write(line + "\n");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayBasket(JFrame frame) {
        frame.getContentPane().removeAll();
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        final int[] total = {0};

        ModernButton backButton = new ModernButton("Back");
        backButton.addActionListener(e -> new MainDisplay().display(frame, id));
        frame.add(backButton);

        for (String key : basket.keySet()) {
            Integer[] values = basket.get(key);

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.setPreferredSize(new Dimension(1000, 30));
            panel.setBackground(Color.BLACK);

            JLabel label = new JLabel(key);
            label.setFont(new Font("Arial", Font.PLAIN, 30));
            label.setForeground(Color.WHITE);
            panel.add(label);

            JLabel label2 = new JLabel("Price: £" + values[0] + " Quantity: " + values[1]);
            label2.setFont(new Font("Arial", Font.PLAIN, 30));
            label2.setForeground(Color.WHITE);
            panel.add(label2);

            frame.add(panel);
            total[0] += values[0] * values[1];
        }

        ModernButton checkoutButton = new ModernButton("Checkout");
        checkoutButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Order Placed! Total: £" + total[0], "Success", JOptionPane.INFORMATION_MESSAGE);
            try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
                String line;
                ArrayList<String> content = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts[0].equals(id)) {
                        Integer spent = Integer.parseInt(parts[3]);
                        spent += total[0];
                        String toAdd = parts[4].substring(0, parts[4].length() - 1);
                        for (String key : basket.keySet()) {
                            Integer[] values2 = basket.get(key);
                            toAdd += key + "=" + values2[0] + ":" + values2[1] + "|";
                        }
    
                        toAdd = toAdd.substring(0, toAdd.length() - 1);
                        toAdd += "}";

                        content.add(parts[0] + "," + parts[1] + ",{}," + spent + "," + toAdd);
                    } else {
                        content.add(line);
                    }
                }
                try (FileWriter writer = new FileWriter("users.csv")) {
                    writer.write("");
                    for (String line2 : content) {
                        writer.write(line2 + "\n");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                MainDisplay mainDisplay = new MainDisplay();
                mainDisplay.display(frame, id);
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            basket.clear();
        });
        frame.add(checkoutButton);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setPreferredSize(new Dimension(1000, 30));
        JLabel label = new JLabel("Total: £" + total[0]);
        label.setFont(new Font("Arial", Font.PLAIN, 30));
        panel.add(label);
        panel.setBackground(Color.black);
        frame.add(panel);

        frame.revalidate();
        frame.repaint();
    }
}

class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String filePath) {
        backgroundImage = new ImageIcon(filePath).getImage();
        if (backgroundImage == null) {
            System.out.println("Image not found");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}

class Profile{
    void display(JFrame frame, String id){
        frame.getContentPane().removeAll();

        JPanel panel1 = new BackgroundPanel("background.png");
        panel1.setMaximumSize(new Dimension(1000, 100)); 
        panel1.setLayout(new BorderLayout());
        
        ModernButton logoutButton = new ModernButton("Back");
        logoutButton.addActionListener(e -> new MainDisplay().display(frame, id));
        panel1.add(logoutButton, BorderLayout.WEST);

        ModernButton basketButton = new ModernButton("Basket");
        basketButton.addActionListener(e -> new Basket(id).displayBasket(frame));
        panel1.add(basketButton, BorderLayout.EAST);

        frame.add(panel1, BorderLayout.NORTH);

        JPanel panel2 = new BackgroundPanel("profile.jpg");
        panel2.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(id)) {
                    JLabel label = new JLabel("Greetings " + parts[0]);
                    label.setFont(new Font("Monospace", Font.BOLD, 30));
                    label.setForeground(Color.WHITE);
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    panel2.add(label, gbc);
                    
                    JLabel label2 = new JLabel("Total Spent: £" + parts[3]);
                    label2.setFont(new Font("Monospace", Font.BOLD, 30));
                    label2.setForeground(Color.WHITE);
                    gbc.gridx = 0;
                    gbc.gridy = 1;
                    panel2.add(label2, gbc);


                    JLabel label3 = new JLabel("Previous Orders: ");
                    label3.setFont(new Font("Monospace", Font.BOLD, 30));
                    label3.setForeground(Color.WHITE);
                    gbc.gridx = 0;
                    gbc.gridy = 2;
                    panel2.add(label3, gbc);

                    try{
                    System.out.println(parts[4]);
                    String dictionary = parts[4].substring(1, parts[4].length() - 1); 
                    System.out.println(dictionary);
                        String[] pairs = dictionary.split("\\|"); 
                        
                        for (String pair : pairs) {
                            String[] values = pair.split("="); 
                            if (values.length != 2) continue; 
                            
                            String key = values[0].trim();
                            String[] valueParts = values[1].split(":");

                            if (valueParts.length != 2) continue;
                            
                            Integer firstValue = Integer.parseInt(valueParts[0].trim());
                            Integer secondValue = Integer.parseInt(valueParts[1].trim());
                            
                            JLabel label4 = new JLabel(key + " Price: £" + firstValue + " Quantity: " + secondValue);
                            label4.setFont(new Font("Monospace", Font.BOLD, 30));
                            label4.setForeground(Color.WHITE);
                            gbc.gridx = 0;
                            gbc.gridy += 1;
                            panel2.add(label4, gbc);

                    }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                    
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        frame.add(panel2, BorderLayout.NORTH);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);

    }
}

class MainDisplay {
    private final Inventory inventory1 = new Inventory();
    private final HashMap<String, Integer[]> inventory = inventory1.inventoryDisplay();

    public void display(JFrame frame, String id) {
        Basket basket = new Basket(id);
        frame.getContentPane().removeAll();
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        JPanel panel1 = new BackgroundPanel("background.png");
        panel1.setMaximumSize(new Dimension(1000, 100)); 
        panel1.setLayout(new BorderLayout());

        JPanel logoutHolder = new JPanel();

        ModernButton user = new ModernButton(id);
        logoutHolder.setOpaque(false);
        logoutHolder.add(user);
        user.addActionListener(e -> new Profile().display(frame, id));
        
        ModernButton logoutButton = new ModernButton("Logout");
        logoutButton.addActionListener(e -> new Login().display(frame));
        logoutHolder.add(logoutButton);

        panel1.add(logoutHolder, BorderLayout.WEST);

        ModernButton basketButton = new ModernButton("Basket");
        basketButton.addActionListener(e -> basket.displayBasket(frame));
        panel1.add(basketButton, BorderLayout.EAST);

        frame.add(panel1, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        for (String key : inventory.keySet()) {
            Integer[] values = inventory.get(key);

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.setMaximumSize(new Dimension(1000, 80));
            panel.setBackground(Color.BLACK);
            panel.setForeground(Color.WHITE);

            JLabel label = new JLabel(key+ " ");
            label.setFont(new Font("Arial", Font.PLAIN, 30));
            label.setForeground(Color.WHITE);
            panel.add(label, BorderLayout.WEST);


            JLabel label2 = new JLabel("Price: £" + values[0] + " Stock: " + values[1]);
            label2.setFont(new Font("Arial", Font.PLAIN, 30));
            label2.setForeground(Color.WHITE);
            panel.add(label2);

            ModernButton button = new ModernButton("Order");
            panel.add(button, BorderLayout.EAST);

            button.addActionListener(e -> {
                if (values[1] > 0) {
                    values[1] -= 1; 
                    label2.setText("Price: £" + values[0] + " Stock: " + values[1]);
                    inventory1.updateInventory(inventory);
                    basket.addToBasket(key, new Integer[]{values[0], 1}, id);
                } else {
                    JOptionPane.showMessageDialog(frame, "Out of Stock!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            contentPanel.add(panel);
            panel.setBorder(BorderFactory.createLineBorder(Color.black));
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        frame.add(scrollPane, BorderLayout.CENTER);
    

        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }
}

class Admin {
    private final Inventory inventory1 = new Inventory();
    private final HashMap<String, Integer[]> inventory = inventory1.inventoryDisplay();

    public void display(JFrame frame) {
        frame.getContentPane().removeAll();
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        JPanel panel1 = new BackgroundPanel("background.png");
        panel1.setMaximumSize(new Dimension(1000, 100)); 
        panel1.setLayout(new BorderLayout());

        ModernButton logoutButton = new ModernButton("Logout");
        logoutButton.addActionListener(e -> new Login().display(frame));
        panel1.add(logoutButton, BorderLayout.WEST);

        frame.add(panel1, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        for (String key : inventory.keySet()) {
            Integer[] values = inventory.get(key);

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.setMaximumSize(new Dimension(1000, 80));
            panel.setBackground(Color.black);

            JLabel label = new JLabel(key+ " ");
            label.setFont(new Font("Arial", Font.PLAIN, 30));
            label.setForeground(Color.WHITE);
            panel.add(label, BorderLayout.WEST);


            JLabel label2 = new JLabel("Price: £" + values[0] + " Stock: " + values[1]);
            label2.setFont(new Font("Arial", Font.PLAIN, 30));
            label2.setForeground(Color.WHITE);
            panel.add(label2);

            JPanel manage = new JPanel();

            ModernButton button = new ModernButton("Remove");;
            manage.add(button);
            button.addActionListener(e -> {
                if (values[1] > 0) {
                    values[1] -= 1; 
                    label2.setText("Price: £" + values[0] + " Stock: " + values[1]);
                    inventory1.updateInventory(inventory);
                } else {
                    JOptionPane.showMessageDialog(frame, "Out of Stock!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            ModernButton button2 = new ModernButton("Add");
            button2.setFont(new Font("Arial", Font.PLAIN, 20));
            manage.add(button2);
            button2.addActionListener(e -> {
                values[1] += 1; 
                label2.setText("Price: £" + values[0] + " Stock: " + values[1]);
                inventory1.updateInventory(inventory);
            });

            panel.add(manage, BorderLayout.EAST);
            contentPanel.add(panel);
            panel.setBorder(BorderFactory.createLineBorder(Color.black));
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        frame.add(scrollPane, BorderLayout.CENTER);
    

        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }
}

class ModernButton extends JButton {
    private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;

    public ModernButton(String text) {
        super(text);
        setFont(new Font("Monospaced", Font.BOLD, 20));
        setForeground(Color.WHITE);
        setBackground(new Color(30, 30, 30));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        hoverBackgroundColor = new Color(50, 50, 50);  
        pressedBackgroundColor = new Color(20, 20, 20); 

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverBackgroundColor);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(new Color(30, 30, 30));
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(pressedBackgroundColor);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setBackground(hoverBackgroundColor);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background with rounded corners
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);  // Rounded edges

        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
    }
}

class Login {
    public void display(JFrame frame) {
        frame.getContentPane().removeAll();
        JPanel panel = new BackgroundPanel("login.png");
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel label = new JLabel("Welcome to Order App");
        label.setFont(new Font("Monospaced", Font.BOLD, 50));
        label.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(label, gbc);

        JLabel label2 = new JLabel("Username:");
        label2.setFont(new Font("Monospaced", Font.PLAIN, 30));
        label2.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(label2, gbc);

        JTextField username = new JTextField(20);
        username.setFont(new Font("Monospaced", Font.PLAIN, 30));
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(username, gbc);

        JLabel label3 = new JLabel("Password:");
        label3.setFont(new Font("Monospaced", Font.PLAIN, 30));
        label3.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(label3, gbc);

        JPasswordField password = new JPasswordField(20);
        password.setFont(new Font("Monospaced", Font.PLAIN, 30));
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(password, gbc);

        ModernButton loginButton = new ModernButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        loginButton.addActionListener(e -> 
        {
            String user = username.getText();
            String pass = new String(password.getPassword());
            if (user.equals("admin") && pass.equals("admin")) {
                String id = "Admin";
                new Admin().display(frame);
            } else {
                try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
                    String line;
                    int flag = 1;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        if (user.equals(parts[0])){
                            if (pass.equals(parts[1])) {
                                String id = parts[0];
                                new MainDisplay().display(frame, id);
                                flag = 0;
                            }
                        }
                }
                if (flag == 1) {
                    JOptionPane.showMessageDialog(frame, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
                    }
            ModernButton registerButton = new ModernButton("Register");
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            panel.add(registerButton, gbc);
                    
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        ModernButton registerButton = new ModernButton("Register");
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            panel.add(registerButton, gbc);
            registerButton.addActionListener(e -> {
                new Register().display(frame);
            });

        frame.add(panel);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);

    }

}

class Register{
    public void display(JFrame frame){
        frame.getContentPane().removeAll();
        JPanel panel = new BackgroundPanel("login.png");
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel label = new JLabel("Register");
        label.setFont(new Font("Monospaced", Font.BOLD, 50));
        label.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(label, gbc);

        JLabel label2 = new JLabel("Username:");
        label2.setFont(new Font("Monospaced", Font.PLAIN, 30));
        label2.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(label2, gbc);
        JTextField username = new JTextField(20);
        username.setFont(new Font("Monospaced", Font.PLAIN, 30));
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(username, gbc);

        JLabel label3 = new JLabel("Password:");
        label3.setFont(new Font("Monospaced", Font.PLAIN, 30));
        label3.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(label3, gbc);
        JPasswordField password = new JPasswordField(20);
        password.setFont(new Font("Monospaced", Font.PLAIN, 30));
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(password, gbc);
        
        ModernButton registerButton = new ModernButton("Register");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(registerButton, gbc);
        registerButton.addActionListener(e -> {

            ArrayList<String> users = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    users.add(parts[0]);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            String user = username.getText();
            String pass = new String(password.getPassword());
            
            if (users.contains(user)) {
                JOptionPane.showMessageDialog(frame, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Username or password cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (FileWriter writer = new FileWriter("users.csv", true)) {
                writer.write(user + "," + pass + ",{}," + 0 + ",{}\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            new Login().display(frame);
        });
        

        frame.add(panel);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);

    }
}

public class OrderApp {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Order App");
        frame.setSize(1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        new Login().display(frame);
        
    }
}
