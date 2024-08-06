package net.ellapiz.productos_view;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyConfig implements WebMvcConfigurer {
	@Value("${images.path}")
	private String path 	= "";
	@Value("${files.path}")
	private String pathFiles = "";
	
	/**
	 * Este manejador de recursos(ResourHandler), me permite agregar una
	 * carpeta externa al jar desde una url /myimages/** para poder tener acceso
	 * a las imagenes guardadas en el path que resuelve /var/test en el caso de estar en desarrollo
	 * en produccion, se modifica via el application.properties
	 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
       registry
               .addResourceHandler("/myimages/**","/myfiles/**")
               .addResourceLocations(path,pathFiles);
       
    }
   
}