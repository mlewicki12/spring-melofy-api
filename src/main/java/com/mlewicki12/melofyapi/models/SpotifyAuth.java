package com.mlewicki12.melofyapi.models;

import com.google.gson.annotations.SerializedName;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
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
    private String access_token;

    @SerializedName("token_type")
    private String token_type;

    @SerializedName("scope")
    private String scope;

    @SerializedName("expires_in")
    private int expires_in;

    @SerializedName("refresh_token")
    private String refresh_token;

    public UUID getUuid() { return this.uuid; }

    public String getAccessToken() { return this.access_token; }
    public void setAccessToken(String access_token) { this.access_token = access_token; }

    public String getTokenType() { return this.token_type; }
    public void setTokenType(String token_type) { this.token_type = token_type; }

    public String getScope() { return this.scope; }
    public void setScope(String scope) { this.scope = scope; }

    public int getExpiresIn() { return this.expires_in; }
    public void setExpiresIn() { this.expires_in = expires_in; }

    public String getRefreshToken() { return this.refresh_token; }
    public void setRefreshToken(String refresh_token) { this.refresh_token = refresh_token; }

    public String toString() {
        return String.format("[\n\taccess_token: %s,\n\ttoken_type: %s,\n\tscope: %s,\n\texpires_in: %d,\n\trefresh_token: %s\n]",
                                    this.access_token, this.token_type, this.scope, this.expires_in, this.refresh_token);
    }
}
