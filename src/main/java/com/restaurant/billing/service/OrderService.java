package com.restaurant.billing.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.restaurant.billing.dto.BillResponse;
import com.restaurant.billing.dto.OrderItemRequest;
import com.restaurant.billing.entity.MenuItem;
import com.restaurant.billing.entity.Order;
import com.restaurant.billing.entity.OrderItem;
import com.restaurant.billing.repository.MenuItemRepository;
import com.restaurant.billing.repository.OrderItemRepository;
import com.restaurant.billing.repository.OrderRepository;
import com.restaurant.billing.util.PdfGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
	@Autowired
	private final OrderRepository orderRepo;
	@Autowired
	private final MenuItemRepository menuRepo;
	private final OrderItemRepository itemRepo;

	@Autowired
	private EmailService emailService;

	public Order createOrder(){
	    Order order = new Order();
	    order.setStatus("CREATED");
	    order.setPaymentStatus("PENDING");
	    return orderRepo.save(order);
	}

	public Order addItem(Long orderId, Long menuItemId, int qty) {
		Order order = orderRepo.findById(orderId).orElseThrow();

		MenuItem menuItem = menuRepo.findById(menuItemId).orElseThrow();

		OrderItem item = OrderItem.builder().order(order).menuItem(menuItem).quantity(qty)
				.price(menuItem.getPrice() * qty).build();

		itemRepo.save(item);

		order.getItems().add(item);

		order.setTotalAmount(order.getTotalAmount() + item.getPrice());

		return orderRepo.save(order);
	}

	public Order checkOut(Long orderId) {
		Order order = orderRepo.findById(orderId).orElseThrow();

//		order.setStatus("PAID");
		order.setStatus("COMPLETED");
		Order saved = orderRepo.save(order);
		
		//generate pdf
		byte[] pdf = PdfGenerator.generate(saved);
		
		//send email if provided
		if(saved.getCustomerEmail()!=null) {
			emailService.sendBill(saved.getCustomerEmail(), pdf);
		}
		return saved;
	}

	public Order updateQuantity(Long orderId, Long itemId, int qty) {
		OrderItem item = itemRepo.findById(itemId).orElseThrow();

		Order order = item.getOrder();

		// subtract old price
		order.setTotalAmount(order.getTotalAmount() - item.getPrice());

		// update values
		item.setQuantity(qty);
		item.setPrice(item.getMenuItem().getPrice() * qty);

		itemRepo.save(item);

		// add new price
		order.setTotalAmount(order.getTotalAmount() + item.getPrice());
		return orderRepo.save(order);
	}

	public Order getOrder(Long id) {
		return orderRepo.findById(id).orElseThrow(() -> new RuntimeException("Order Not Found"));
	}

	public BillResponse calculateBill(Long orderId) {
		Order order = orderRepo.findById(orderId).orElseThrow();

		double subtotal = order.getTotalAmount();

		double tax = subtotal * 0.05; // 5% Tax
		double discount = subtotal > 500 ? subtotal * 0.10 : 0; // 10% discount if > 500

		double grandTotal = subtotal + tax - discount;

		return BillResponse.builder().subtotal(subtotal).tax(tax).discount(discount).grandTotal(grandTotal).build();
	}
	
	public byte[] generateBillPdf(Long orderId) {
		Order order = orderRepo.findById(orderId)
				.orElseThrow();
		
		return PdfGenerator.generate(order);
	}
	public Order addItem(Long orderId, OrderItemRequest request){

	    Order order = orderRepo.findById(orderId)
	            .orElseThrow(() -> new RuntimeException("Order not found"));

	    MenuItem item = menuRepo.findById(request.getMenuItemId())
	            .orElseThrow(() -> new RuntimeException("Item not found"));

	    OrderItem orderItem = new OrderItem();
	    orderItem.setMenuItem(item);
	    orderItem.setQuantity(request.getQuantity());
	    orderItem.setOrder(order);

	    order.getItems().add(orderItem);

	    return orderRepo.save(order);
	}
	public Order checkout(Long orderId){

	    Order order = orderRepo.findById(orderId)
	            .orElseThrow(() -> new RuntimeException("Order not found"));

	    order.setStatus("COMPLETED");

	    return orderRepo.save(order);
	}
	
	public Order makePayment(Long orderId, String method){

	    Order order = orderRepo.findById(orderId)
	            .orElseThrow(() -> new RuntimeException("Order not found"));

	    if(!order.getStatus().equals("COMPLETED")){
	        throw new RuntimeException("Order not checked out yet");
	    }

	    order.setPaymentStatus("PAID");
	    order.setPaymentMethod(method);

	    return orderRepo.save(order);
	}



}
