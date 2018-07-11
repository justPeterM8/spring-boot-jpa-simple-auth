package pl.asap.asapbe.services;

import pl.asap.asapbe.entities.UserAuthDetailsEntity;
import pl.asap.asapbe.entities.UserEntity;

public interface UserAuthDetailsService {

    UserAuthDetailsEntity getUserAuthDetailsFromUserEntity(UserEntity userEntity);
}