package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    private HashMap<String, Order> orderMap;
    private HashMap<String, DeliveryPartner> partnerMap;
    private HashMap<String, HashSet<String>> partnerToOrderMap;
    private HashMap<String, String> orderToPartnerMap;
    private HashSet<String> notAssignOrders;
    public OrderRepository(){
        this.orderMap = new HashMap<String, Order>();
        this.partnerMap = new HashMap<String, DeliveryPartner>();
        this.partnerToOrderMap = new HashMap<String, HashSet<String>>();
        this.orderToPartnerMap = new HashMap<String, String>();
        this.notAssignOrders = new HashSet<>();
    }

    public void saveOrder(Order order){
        // your code here
        orderMap.put(order.getId(), order);
        notAssignOrders.add(order.getId());
    }

    public void savePartner(String partnerId){
        // your code here
        DeliveryPartner partner = new DeliveryPartner(partnerId);
        partnerMap.put(partnerId,partner);

        // create a new partner with given partnerId and save it
    }

    public void saveOrderPartnerMap(String orderId, String partnerId){
        if(orderMap.containsKey(orderId) && partnerMap.containsKey(partnerId)){
            // your code here
           // remove order from not assigned
            notAssignOrders.remove(orderId);
            HashSet<String> ordersForPartner = partnerToOrderMap.getOrDefault(partnerId, new HashSet<>());
            ordersForPartner.add(orderId);
            partnerToOrderMap.put(partnerId, ordersForPartner);
            orderToPartnerMap.put(orderId,partnerId);

            DeliveryPartner partner = partnerMap.get(partnerId);
            partner.setNumberOfOrders(partner.getNumberOfOrders() + 1);

            // Assign partner to this order
            orderToPartnerMap.put(orderId, partnerId);

            //add order to given partner's order list
            //increase order count of partner
            //assign partner to this order
        }
    }

    public Order findOrderById(String orderId){
        // your code here
        return orderMap.getOrDefault(orderId, null);
    }

    public DeliveryPartner findPartnerById(String partnerId){
        return partnerMap.getOrDefault(partnerId,null);
    }

    public Integer findOrderCountByPartnerId(String partnerId){
        // your code here
        return partnerMap.get(partnerId).getNumberOfOrders();
    }

    public List<String> findOrdersByPartnerId(String partnerId){
        // your code here
        List<String> orders = new ArrayList<>();

        // Retrieve the set of order IDs associated with the given partnerId
        HashSet<String> orderIds = partnerToOrderMap.getOrDefault(partnerId, new HashSet<>());

        // Add each order ID to the list of orders
        for (String orderId : orderIds) {
            orders.add(orderId);
        }

        return orders;
    }

    public List<String> findAllOrders(){
        // your code here
        // return list of all orders
        List<String> allOrders = new ArrayList<>(orderMap.keySet());
        return allOrders;
    }

    public void deletePartner(String partnerId){
        // your code here
        // delete partner by ID
        if (partnerMap.containsKey(partnerId)) {
            // Remove the partner from partnerMap
            partnerMap.remove(partnerId);

            // Check if the partner has any assigned orders
            if (partnerToOrderMap.containsKey(partnerId)) {
                // Get the set of order IDs assigned to this partner
                HashSet<String> ordersForPartner = partnerToOrderMap.get(partnerId);

                // Iterate over the order IDs and unassign the partner from each order
                for (String orderId : ordersForPartner) {
                    // Remove the partner assignment from orderToPartnerMap
                    notAssignOrders.add(orderId);
                    orderToPartnerMap.remove(orderId);

                    // Decrement the order count of the partner for each assigned order
                    DeliveryPartner partner = partnerMap.get(partnerId);
                    if (partner != null) {
                        partner.setNumberOfOrders(partner.getNumberOfOrders() - 1);
                    }
                }

                // Remove the partner's entry from partnerToOrderMap
                partnerToOrderMap.remove(partnerId);
            }
        }

    }

    public void deleteOrder(String orderId){
        // your code here
        // delete order by ID
        if (orderMap.containsKey(orderId)) {
            // Remove the order from orderMap
            orderMap.remove(orderId);
            notAssignOrders.remove(orderId);
            // Check if the order was assigned to a partner
            if (orderToPartnerMap.containsKey(orderId)) {
                String partnerId = orderToPartnerMap.get(orderId);

                // Remove the order from the partner's order list
                HashSet<String> ordersForPartner = partnerToOrderMap.get(partnerId);
                if (ordersForPartner != null) {
                    ordersForPartner.remove(orderId);
                }

                // Decrement the order count of the partner
                DeliveryPartner partner = partnerMap.get(partnerId);
                if (partner != null) {
                    partner.setNumberOfOrders(partner.getNumberOfOrders() - 1);
                }

                // Unassign the partner from this order
                orderToPartnerMap.remove(orderId);
            }
        }
    }

    public Integer findCountOfUnassignedOrders(){
        // your code here
        return notAssignOrders.size();
    }

    public Integer findOrdersLeftAfterGivenTimeByPartnerId(String timeString, String partnerId){
        // your code here
        int givenTime = convertDeliveryTimeToInt(timeString);

        // Initialize count of orders left after given time
        int ordersLeft = 0;

        // Get the set of order IDs assigned to the given partner
        HashSet<String> ordersForPartner = partnerToOrderMap.getOrDefault(partnerId, new HashSet<>());

        // Iterate over the order IDs to check if they are left undelivered after the given time
        for (String orderId : ordersForPartner) {
            int orderDeliveryTime = orderMap.get(orderId).getDeliveryTime();

            // If the order delivery time is after the given time, increment ordersLeft
            if (orderDeliveryTime > givenTime) {
                ordersLeft++;
            }
        }

        return ordersLeft;
    }

    public String findLastDeliveryTimeByPartnerId(String partnerId){
        // your code here
        // code should return string in format HH:MM
        int maxDeliveryTime = Integer.MIN_VALUE;
        String lastDeliveryTime = "";

        // Get the set of order IDs assigned to the given partner
        HashSet<String> ordersForPartner = partnerToOrderMap.getOrDefault(partnerId, new HashSet<>());

        // Iterate over the order IDs to find the maximum delivery time
        for (String orderId : ordersForPartner) {
            int orderDeliveryTime = orderMap.get(orderId).getDeliveryTime();

            // Update the maximum delivery time if the current order delivery time is greater
            if (orderDeliveryTime > maxDeliveryTime) {
                maxDeliveryTime = orderDeliveryTime;
            }
        }

        // Convert the maximum delivery time to HH:MM format
        int hours = maxDeliveryTime / 60;
        int minutes = maxDeliveryTime % 60;
        lastDeliveryTime = String.format("%02d:%02d", hours, minutes);

        return lastDeliveryTime;
    }
    private int convertDeliveryTimeToInt(String deliveryTime) {
        String[] timeParts = deliveryTime.split(":");
        int hours = Integer.parseInt(timeParts[0]);
        int minutes = Integer.parseInt(timeParts[1]);
        return hours * 60 + minutes;
    }
}