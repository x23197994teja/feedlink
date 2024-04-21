package com.teja.feedlink.service;

import com.teja.feedlink.entity.Property;
import com.teja.feedlink.model.ResponseModel;
import com.teja.feedlink.repository.PropertyRepository;
import com.teja.feedlink.repository.UserRepository;
import com.teja.feedlink.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static com.teja.feedlink.config.Constants.STATUS_ACTIVE;
import static java.util.Objects.nonNull;


@Service
public class PropertyService {

    @Autowired
    PropertyRepository propertyRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private JavaMailSender emailSender;

    public List<Property> fetchProducts() {
        return propertyRepository.findByStatus(STATUS_ACTIVE);
    }

    public List<Property> fetchMyProducts(String userId) {
        return propertyRepository.findByStatusAndPostedBy(STATUS_ACTIVE, userId);
    }

    @Value("${s3ImageUrl}")
    private String s3ImageUrl;

    public ResponseModel saveProduct(Property property, String userName) {
        ResponseModel response = new ResponseModel();
        try {
            property.setId(UUID.randomUUID());
            property.setStatus("A");
            property.setPostedOn(LocalDateTime.now());
            property.setPostedBy(userName);
            propertyRepository.save(property);
            response.setStatus("Success");
            response.setMessage("Operation Successful");
            response.setCreationId(property.getId().toString());
        } catch (Exception e) {
            response.setException(e.getLocalizedMessage());
        }
        return response;
    }

    public ResponseModel updateProperty(UUID propertyId, Property property, String userName) {
        ResponseModel response = new ResponseModel();
        try {
            propertyRepository.findById(propertyId).ifPresent(
                    savedProperty -> {
                        if (savedProperty.getPostedBy().equals(userName)) {
                            setUpdatedValues(savedProperty, property);
                            propertyRepository.save(savedProperty);
                            response.setStatus("Success");
                            response.setMessage("Operation Successful");
                        } else {
                            response.setStatus("Unsuccessful");
                            response.setMessage("Operation Unsuccessful : Not Authorized To Update This Property");
                        }
                    }
            );
        } catch (Exception e) {
            response.setStatus("Unsuccessful");
            response.setMessage("Exception Occurred");
            response.setException(e.getLocalizedMessage());
        }
        return response;
    }

    private void setUpdatedValues(Property property, Property updatedProp) {
        if (nonNull(updatedProp.getAddress()))
            property.setAddress(updatedProp.getAddress());

        if (nonNull(updatedProp.getFeatures()))
            property.setFeatures(updatedProp.getFeatures());

        if (nonNull(updatedProp.getName()))
            property.setName(updatedProp.getName());

        if (nonNull(updatedProp.getPrice()))
            property.setPrice(updatedProp.getPrice());

        if (nonNull(updatedProp.getPostalCode()))
            property.setPostalCode(updatedProp.getPostalCode());

        if (nonNull(updatedProp.getYearMade()))
            property.setYearMade(updatedProp.getYearMade());

        if (nonNull(updatedProp.getDescription()))
            property.setDescription(updatedProp.getDescription());

        property.setModifiedOn(LocalDateTime.now());
    }

    public ResponseModel inactiveProperty(UUID propertyId, String userName) {
        ResponseModel response = new ResponseModel();
        try {
            propertyRepository.findById(propertyId).ifPresent(
                    savedProperty -> {
                        if (savedProperty.getPostedBy().equals(userName)) {
                            savedProperty.setStatus("I");
                            propertyRepository.save(savedProperty);
                            response.setStatus("Success");
                            response.setMessage("Operation Successful");
                        } else {
                            response.setStatus("Unsuccessful");
                            response.setMessage("Operation Unsuccessful : Not Authorized To Update This Property");
                        }
                    }
            );
        } catch (Exception e) {
            response.setStatus("Unsuccessful");
            response.setMessage("Exception Occurred");
            response.setException(e.getLocalizedMessage());
        }
        return response;
    }

    @Transactional
    public void updateImageUrl(String propertyId, String fileKey) {
        propertyRepository.findById(UUID.fromString(propertyId)).ifPresent(
                product -> {
                    if (nonNull(product.getImages())) {
                        product.getImages().add(s3ImageUrl + fileKey);
                    } else {
                        product.setImages(List.of(s3ImageUrl + fileKey));
                    }
                    propertyRepository.save(product);
                }
        );
    }

    public ResponseModel emailPropertyOwner(UUID productId, String userName) {
        ResponseModel response = new ResponseModel();
        Property prod = propertyRepository.findById(productId).orElse(null);
        User productUser = null;
        if (nonNull(prod)) {
            productUser = userRepository.findByUsername(prod.getPostedBy()).orElse(null);
        }
        User enquiryUser = userRepository.findByUsername(userName).orElse(null);
        if (nonNull(productUser) && nonNull(enquiryUser)) {
            sendEmail(productUser, enquiryUser, prod);
        }
        response.setStatus("Success");
        response.setMessage("Operation Successful");
        return response;
    }

    private void sendEmail(User productUser, User enquiryUser, Property property) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tejagadikota7@gmail.com");
        message.setTo(productUser.getEmail());
        message.setSubject("Enquiry Related To Pick UP Of Donation Posted By : " + property.getName());

        String emailMsg = "Dear " + property.getName() + ",\n\n" +
                "A potential receiver has shown interest in receiving food donations through Feedlink. We kindly request that you reach out to the receiver to discuss the food donation details.\n\n" +
                "Here are the details of the enquirer:\n\n" +
                " Name: " + enquiryUser.getFirstname() + " " + enquiryUser.getLastname() + "\n" +
                " Email ID: " + enquiryUser.getEmail() + "\n\n" +
                "Please contact the receiver at your earliest convenience to discuss the food donation process and any necessary arrangements. If you have any questions or need assistance, feel free to reach out to us.\n\n" +
                "Thank you for your commitment to supporting cattle welfare through Feedlink.\n\n" +
                "Best regards,\n\n" +
                "Team Feedlink";

        message.setText(emailMsg);
        this.emailSender.send(message);
    }
}
