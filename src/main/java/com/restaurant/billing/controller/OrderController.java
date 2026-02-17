package com.restaurant.billing.controller;

import com.restaurant.billing.dto.OrderItemRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.billing.dto.BillResponse;
import com.restaurant.billing.dto.CreateOrderRequest;
import com.restaurant.billing.entity.Order;
import com.restaurant.billing.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
	private final OrderService service;

	@PostMapping
	public Order create(@RequestBody CreateOrderRequest request){
	    return service.createOrder(request);
	}


	@PostMapping("/{orderId}/add")
	public Order addItem(@PathVariable Long orderId, @RequestParam Long itemId, @RequestParam int qty) {

		return service.addItem(orderId, itemId, qty);
	}

	@PostMapping("/{orderId}/checkOut")
	public Order checkOut(@PathVariable Long orderId) {
		return service.checkOut(orderId);
	}

	@PutMapping("/{orderId}/update-item")
	public Order updateQty(@PathVariable Long orderId, @RequestParam Long itemId, @RequestParam int qty) {
		return service.updateQuantity(orderId, itemId, qty);
	}

	@GetMapping("/{id}")
	public Order getOrder(@PathVariable Long id) {
		return service.getOrder(id);
	}

	@GetMapping("/{id}/bill")
	public BillResponse getBill(@PathVariable Long id) {
		return service.calculateBill(id);
	}

	@GetMapping("/{id}/pdf")
	public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
		byte[] pdf = service.generateBillPdf(id);

		return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=bill_" + id + ".pdf")
				.contentType(MediaType.APPLICATION_PDF).body(pdf);

	}
	
	@PostMapping("/{id}/add-item")
	public Order addItem(@PathVariable Long id,
	                     @RequestBody OrderItemRequest request) {
	    return service.addItem(id, request);
	}

	@PostMapping("/{id}/checkout")
	public Order checkout(@PathVariable Long id){
	    return service.checkout(id);
	}

	@PostMapping("/{id}/pay")
	public Order pay(@PathVariable Long id,
	                 @RequestParam String method){
	    return service.makePayment(id, method);
	}

	//added temp just to check
//	@GetMapping("/test-mail")
//	public String testMail(){
//	    service.sendBill("jayeshborase701@gmail.com", new byte[10]);
//	    return "sent";
//	}

}
