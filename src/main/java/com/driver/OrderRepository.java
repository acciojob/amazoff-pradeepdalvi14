package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;


public class OrderRepository
{
    HashMap<String, Order>orderDB=new HashMap<>();
    HashMap<String, DeliveryPartner>deliveryPartnerDB=new HashMap<>();

    HashMap<String, List<String>>partnerListOfOrders=new HashMap<>(); //partnerId -> {List of orders}
    List<String>orders=new ArrayList<>();
    List<String>notAssignedOrders=new ArrayList<>();

    public void addOrder(Order order)
    {
        String id=order.getId();
        orderDB.put(id,order);
        orders.add(id);
        notAssignedOrders.add(id);
    }

    public void addPartner(String partnerId)
    {
        DeliveryPartner deliveryPartner=new DeliveryPartner(partnerId);
        deliveryPartnerDB.put(partnerId,deliveryPartner);
    }

    public void addOrderPartnerPair(String orderId,String partnerId)
    {
        List<String>order;
        if(!partnerListOfOrders.containsKey(partnerId))
        {
            order=new ArrayList<>();
        }
        else
        {
            order=partnerListOfOrders.get(partnerId);
        }

        order.add(orderId);
        partnerListOfOrders.put(partnerId,order);
        DeliveryPartner deliveryPartner=deliveryPartnerDB.get(partnerId);
        deliveryPartner.setNumberOfOrders(deliveryPartner.getNumberOfOrders()+1);
        deliveryPartnerDB.put(partnerId,deliveryPartner);
        notAssignedOrders.remove(orderId);
    }

    public Order getOrderById(String orderId)
    {
        return orderDB.get(orderId);
    }

    public DeliveryPartner getPartnerById(String partnerId)
    {
        return deliveryPartnerDB.get(partnerId);
    }

    public Integer getOrderCountByPartnerId(String partnerId)
    {
        if(!partnerListOfOrders.containsKey(partnerId))
        {
            return 0;
        }
        return partnerListOfOrders.get(partnerId).size();
    }

    public List<String>getOrdersByPartnerId(String partnerId)
    {
        return partnerListOfOrders.get(partnerId);
    }

    public List<String>getAllOrders()
    {
        List<String>ans=new ArrayList<>();
        for(String key:orderDB.keySet())
        {
            Order obj=orderDB.get(key);
            ans.add(obj.getId());
        }
        return ans;
    }

    public Integer getCountOfUnassignedOrders()
    {
        return notAssignedOrders.size();
    }

    public Integer getOrdersLeftAfterGivenTimeByPartnerId(String time, String partnerId)
    {
        List<String>orders=partnerListOfOrders.get(partnerId);
        if(orders==null)
        {
            return 0;
        }

        String arr1[]=time.split(":");
        int hh1=60*Integer.parseInt(arr1[0]);
        int mm1=Integer.parseInt(arr1[1]);
        int myTime=hh1+mm1;

        int count=0;

        for(String id:orders)
        {

            Order obj=orderDB.get(id);

            if(myTime<obj.getDeliveryTime())
            {
                count++;
            }
        }
        return count;
    }

    public String getLastDeliveryTimeByPartnerId(String partnerId)
    {
        List<String>orders=partnerListOfOrders.get(partnerId);
        if(orders==null)
        {
            return null;
        }

        int time=0;

        for(String key:orders)
        {
            Order obj=orderDB.get(key);
            if(time<obj.getDeliveryTime())
            {
                time=obj.getDeliveryTime();
            }
        }

        //make string
        //format: HH:MM
        int mm=time%60;
        String MM=(mm>=0 && mm<=9)?("0"+mm):(mm+"");

        int hh=(time-mm)/60;
        String HH=(hh>=0 && hh<=9)?("0"+hh):(hh+"");

        return HH+":"+MM;
    }

    public void deletePartnerById(String partnerId)
    {
        //Delete the partnerId
        //And push all his assigned orders to unassigned orders.
        if(!deliveryPartnerDB.containsKey(partnerId))
        {
            return;
        }

        List<String>orderList=partnerListOfOrders.get(partnerId);

        deliveryPartnerDB.remove(partnerId);
        partnerListOfOrders.remove(partnerId);

        if(orderList==null || orderList.size()==0)
        {
            return;
        }

        for(String key:orderList)
        {
            orders.remove(key);
            notAssignedOrders.add(key);
        }
    }

    public void deleteOrderById(String orderId)
    {
        //Delete an order and also
        // remove it from the assigned order of that partnerId
        for(List<String>arr:partnerListOfOrders.values())
        {
            if(arr.contains(orderId))
            {
                arr.remove(orderId);
                break;
            }
        }
        orderDB.remove(orderId);
    }

}