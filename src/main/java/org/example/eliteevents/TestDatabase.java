//package org.example.eliteevents;
//
//import org.example.eliteevents.services.DatabaseService;
//import java.sql.ResultSet;
//import java.sql.Statement;
//
//public class TestDatabase {
//    public static void main(String[] args) {
//        try {
//            DatabaseService db = DatabaseService.getInstance();
//
//            // Test clients table
//            Statement stmt = db.getConnection().createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM clients");
//            if (rs.next()) {
//                System.out.println("✅ Clients in database: " + rs.getInt("count"));
//            }
//
//            // Test venues table
//            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM venues");
//            if (rs.next()) {
//                System.out.println("✅ Venues in database: " + rs.getInt("count"));
//            }
//
//            db.close();
//
//        } catch (Exception e) {
//            System.err.println("❌ Test failed: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}