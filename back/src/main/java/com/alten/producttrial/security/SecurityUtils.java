package com.alten.producttrial.security;

import com.alten.producttrial.config.AppProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("securityUtils")
public class SecurityUtils {
    
    private final AppProperties appProperties;
    
    public SecurityUtils(AppProperties appProperties) {
        this.appProperties = appProperties;
    }
    
    /**
     * Extrait l'email de l'utilisateur authentifié depuis le SecurityContext.
     * 
     * @return l'email de l'utilisateur authentifié, ou null si non authentifié
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName(); // Dans notre cas, c'est l'email
        }
        return null;
    }
    
    /**
     * Vérifie si l'utilisateur actuel est l'administrateur.
     * Utilisé par @PreAuthorize pour la sécurité au niveau méthode.
     * 
     * @return true si l'utilisateur est l'administrateur configuré, false sinon
     */
    public boolean isAdmin() {
        String email = getCurrentUserEmail();
        String adminEmail = appProperties.getAdmin().getEmail();
        return email != null && email.equals(adminEmail);
    }
    
    /**
     * Méthode statique pour compatibilité avec le code existant.
     * Préférer utiliser l'instance du bean pour les nouvelles implémentations.
     */
    public static String getCurrentUserEmailStatic() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }
}
