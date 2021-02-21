package com.mlewicki12.melofyapi.models;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Getter @Setter
public class SpotifyAuth {
    @Id
    @GeneratedValue(generator="UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Type(type = "org.hibernate.type.UUIDCharType")
    @SerializedName("uuid")
    private UUID uuid;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("scope")
    private String scope;

    @SerializedName("expires_in")
    private int expiresIn;

    @SerializedName("refresh_token")
    private String refreshToken;

    public String toString() {
        return String.format("[\n\taccess_token: %s,\n\ttoken_type: %s,\n\tscope: %s,\n\texpires_in: %d,\n\trefresh_token: %s\n]",
                                    this.accessToken, this.tokenType, this.scope, this.expiresIn, this.refreshToken);
    }
}
