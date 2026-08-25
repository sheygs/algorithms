package TokenManager.AuthManager;

import java.util.HashMap;
import java.util.Map;

public class AuthManager {
   private final int timeToLive;
   // tokenId -> expirationTime
   private final Map<String, Integer> tokens;

   public AuthManager(int timeToLive) {
      if (timeToLive <= 0) {
        throw new IllegalArgumentException(
            "timeToLive must be greater than zero"
        );
      }
      this.timeToLive = timeToLive;
      this.tokens = new HashMap<>();
   }

   public void generate(String tokenId, int currentTime) {
      validateToken(tokenId);

      // Token exists and is still valid
      Integer expirationTime = tokens.get(tokenId);
      if (expirationTime != null && expirationTime > currentTime) {
         return;
      }

      expirationTime = currentTime + this.timeToLive;
      tokens.put(tokenId, expirationTime);
   }

   public void renew(String tokenId, int currentTime) {
      validateToken(tokenId);

      // Token does not exist or has already expired
      Integer expirationTime = tokens.get(tokenId);
      if (expirationTime == null || currentTime >= expirationTime) {
         return;
      }

      // renew token
      expirationTime = currentTime + this.timeToLive;
      tokens.put(tokenId, expirationTime);
   }

   private void validateToken(String tokenId) {
      if (tokenId == null || tokenId.isBlank()) {
         throw new IllegalArgumentException("token ID must not be null or empty");
      }
   }

   public int countUnexpiredTokens(int currentTime) {
      int count = 0;
      for (int expirationTime : tokens.values()) {
         if (currentTime < expirationTime) {
            count++;
         }
      }
      return count;
   }
}
