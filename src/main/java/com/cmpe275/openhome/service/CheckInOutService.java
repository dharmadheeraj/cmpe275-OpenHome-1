package com.cmpe275.openhome.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import com.cmpe275.openhome.model.Booking;
import com.cmpe275.openhome.model.Property;
import com.cmpe275.openhome.repository.BookingRepository;
import com.cmpe275.openhome.utils.DateUtility;
import com.cmpe275.openhome.utils.EmailUtility;

@Component
public class CheckInOutService extends QuartzJobBean{
	@Autowired
    private BookingRepository bookingRepository;
	
	@Autowired
	private BookingService bookingService;
	
	@Autowired
	private PropertyService propertyService;
	
	@Autowired
	private EmailService emailService;
	
	private static final Logger logger = LoggerFactory.getLogger(CheckInOutService.class);
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
		logger.info("Executing Job with key {}", context.getJobDetail().getKey());
		logger.info("Executing Job with key {}", context.getJobDetail().getDescription());
		updateBookingAfterCheckin(context.getJobDetail().getDescription());
	}
	
	private void updateBookingAfterCheckin(String jobType) {
		List<Booking> bookingList = bookingRepository.findAll();
		String today = DateUtility.todayDate(0);
		String yesterday = DateUtility.todayDate(-1);
		for(Booking booking : bookingList) {
			if(jobType.equals("check-out") && booking.isUser_checked_in_flag() && !booking.isBooking_cancelled() && today.equals(booking.getCheck_out_date())) {
				booking.setUser_check_out_date(today);
				booking.setUser_checked_out_flag(true);
				sendCheckinoutNotification(EmailUtility.createCheckOutConfirmationMsg() , EmailUtility.createCheckOutConfirmationMsgHost(),
						booking.getHost_email() , booking.getUser_email());
				bookingService.saveBookingDetails(booking);
			}
			else if(jobType.equals("check-in") && yesterday.equals(booking.getCheck_in_date())) {
				if(!booking.isUser_checked_in_flag()) {
					booking.setBooking_cancelled(true);
					booking.setNo_show(true);
					double perDayFine = (booking.getPrice() / booking.getTotal_nights()) * 0.3;
					if(booking.getTotal_nights() >= 2) {
						double totalFine = perDayFine * 2;
						booking.setAmount_paid(totalFine);
					}
					else {
						booking.setAmount_paid(perDayFine);
					}
					sendCancellationNotification(EmailUtility.createCancellationConfirmationMsg() , EmailUtility.createCancellationConfirmationMsgHost(),
								booking.getHost_email() , booking.getUser_email());
					Property property = propertyService.getPropertyById(booking.getProperty_unique_id());
					SimpleDateFormat simpleDF = new SimpleDateFormat("yyyy-MM-dd");
					Date convertedCurrentDate = null;
					try {
						convertedCurrentDate = simpleDF.parse(today);
						property.setBooked_flag(false);
						property.setStartdate(convertedCurrentDate);
						propertyService.savePropertyDetails(property);
						bookingService.saveBookingDetails(booking);
					} catch (ParseException exception) {
						System.out.println(exception.getMessage());
					}
				}
			}
			
		}
	}
	
	public void sendCheckinoutNotification(String guestMessage , String hostMessage, String hostEmail,  String guestEmail) {
		// TODO Auto-generated method stub
		emailService.sendEmail(guestEmail, guestMessage, " Check-In/Check-out Confirmation with OpenHome.!!");
        emailService.sendEmail(hostEmail, hostMessage, " Check-In/Check-out Confirmation with OpenHome.!!");
	}
	
	public void sendCancellationNotification(String guestMessage , String hostMessage, String hostEmail,  String guestEmail) {
		// TODO Auto-generated method stub
		emailService.sendEmail(guestEmail, guestMessage, " Booking Cancelled with OpenHome.!!");
        emailService.sendEmail(hostEmail, hostMessage, " Booked property got cancelled.!!");
	}
}
