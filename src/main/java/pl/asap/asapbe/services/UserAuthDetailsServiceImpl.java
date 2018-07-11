package pl.asap.asapbe.services;

import org.springframework.stereotype.Service;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;
import pl.asap.asapbe.exceptions.UserNotFoundException;
import pl.asap.asapbe.repositories.UserAuthDetailsRepository;

import java.util.Optional;

@Service
public class UserAuthDetailsServiceImpl implements UserAuthDetailsService{

    private UserAuthDetailsRepository userAuthDetailsRepository;

    public UserAuthDetailsServiceImpl(UserAuthDetailsRepository userAuthDetailsRepository) {
        this.userAuthDetailsRepository = userAuthDetailsRepository;
    }

    public UserAuthDetailsEntity getUserAuthDetailsFromUserEntity(UserEntity userEntity){
        Optional<UserAuthDetailsEntity> userAuthDetails = userAuthDetailsRepository.findByUserId(userEntity.getId());
        if (!userAuthDetails.isPresent()){
            throw new UserNotFoundException();
        } else {
            return userAuthDetails.get();
        }
    }
}
