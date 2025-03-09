import { useEffect, useState } from "react"
import { getCurrentUser } from "../utils/api";

export const useCurrentUser = () => {

    const [loading, setLoading] = useState(true);  
    const [currentUser, setCurrentUser] = useState(null);
    
    useEffect(() => {
        setLoading(true);
        //if (!user) {
        const fetchCurrentUser = async () => {
        try {
            // Obtener el token almacenado en localStorage
            const token = window.localStorage.getItem("jwt");
            
            // Verificar si el token existe antes de hacer la solicitud
            if (token) {
                const data = getCurrentUser({token});
                setCurrentUser(data.user);
                localStorage.setItem("user", JSON.stringify(data.user));  // Almacenar datos del usuario
                
            } else {
            setCurrentUser(null); // Si no hay token, no hay usuario autenticado
            }
        } catch (error) {
            console.error("Error fetching current user:", error);
            setCurrentUser(null);
            localStorage.removeItem("user");
        }
        finally{
            setLoading(false);
        }
        };

        const user = JSON.parse(localStorage.getItem("user"));
        if(user){
            setCurrentUser(user);
            setLoading(false);
        }
        else{
            fetchCurrentUser();
        }
        //}
      }, []);
    
    return {currentUser, loading, setCurrentUser}
    
}