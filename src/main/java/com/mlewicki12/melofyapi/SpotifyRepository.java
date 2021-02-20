
package com.mlewicki12.melofyapi;

import com.mlewicki12.melofyapi.models.SpotifyAuth;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SpotifyRepository extends CrudRepository<SpotifyAuth, UUID> {

}
