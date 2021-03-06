package com.myEcommerce.userService.service;

import com.myEcommerce.userService.dto.UserDto;
import com.myEcommerce.userService.jpa.UserEntity;
import com.myEcommerce.userService.jpa.UserRepository;
import com.myEcommerce.userService.vo.ResponseOrder;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

    }

    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));

        userRepository.save(userEntity);

        return userDto;
    }

    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if(userEntity == null) throw new UsernameNotFoundException("User not found");
        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
        List<ResponseOrder> orders = new ArrayList<>();
        userDto.setOrders(orders);
        return userDto;
    }


    public Iterable<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username);
        if(userEntity == null) throw new UsernameNotFoundException(username);
        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
                true, true, true, true, new ArrayList<>());
    }

    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if(userEntity == null) throw new UsernameNotFoundException(email);

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
        return userDto;
    }
}
