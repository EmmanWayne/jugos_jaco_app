package com.jugos_jaco_app.ui.utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationData {
    private static List<String> departamentos;
    private static Map<String, List<String>> municipiosPorDepartamento;

    public static List<String> getDepartamentos() {
        if (departamentos == null) {
            departamentos = Arrays.asList(
                "Seleccione",
                "Atlántida", "Choluteca", "Colón", "Comayagua", "Copán", "Cortés",
                "El Paraíso", "Francisco Morazán", "Gracias a Dios", "Intibucá",
                "Islas de la Bahía", "La Paz", "Lempira", "Ocotepeque", "Olancho",
                "Santa Bárbara", "Valle", "Yoro"
            );
        }
        return departamentos;
    }

    public static Map<String, List<String>> getMunicipiosPorDepartamento() {
        if (municipiosPorDepartamento == null) {
            municipiosPorDepartamento = new HashMap<>();

            municipiosPorDepartamento = new HashMap<>();
            municipiosPorDepartamento.put("Atlántida", Arrays.asList(
                    "Arizona", "El Porvenir", "Esparta", "Jutiapa", "La Ceiba", "La Masica",
                    "San Francisco", "Tela"
            ));
            municipiosPorDepartamento.put("Choluteca", Arrays.asList(
                    "Apacilagua", "Choluteca", "Concepción de María", "Duyure", "El Corpus",
                    "El Triunfo", "Marcovia", "Morolica", "Namasigüe", "Orocuina", "Pespire",
                    "San Antonio de Flores", "San Isidro", "San José", "San Marcos de Colón",
                    "Santa Ana de Yusguare"
            ));
            municipiosPorDepartamento.put("Colón", Arrays.asList(
                    "Balfate", "Bonito Oriental", "Iriona", "Limón", "Sabá", "Santa Fe",
                    "Santa Rosa de Aguán", "Sonaguera", "Tocoa", "Trujillo"
            ));
            municipiosPorDepartamento.put("Comayagua", Arrays.asList(
                    "Ajuterique", "Comayagua", "El Rosario", "Esquías", "Humuya", "La Libertad",
                    "La Trinidad", "Lamaní", "Las Lajas", "Lejamaní", "Meámbar", "Minas de Oro",
                    "Ojos de Agua", "San Jerónimo", "San José de Comayagua", "San José del Potrero",
                    "San Luis", "San Sebastián", "Siguatepeque", "Taulabé", "Villa de San Antonio"
            ));
            municipiosPorDepartamento.put("Copán", Arrays.asList(
                    "Cabañas", "Concepción", "Copán Ruinas", "Corquín", "Cucuyagua", "Dolores",
                    "Dulce Nombre", "El Paraíso", "Florida", "La Jigua", "La Unión", "Nueva Arcadia",
                    "San Agustín", "San Antonio", "San Jerónimo", "San José", "San Juan de Opoa",
                    "San Nicolás", "San Pedro", "Santa Rita", "Santa Rosa de Copán",
                    "Trinidad de Copán", "Veracruz"
            ));
            municipiosPorDepartamento.put("Cortés", Arrays.asList(
                    "Choloma", "La Lima", "Omoa", "Pimienta", "Potrerillos", "Puerto Cortés",
                    "San Antonio de Cortés", "San Francisco de Yojoa", "San Manuel",
                    "Santa Cruz de Yojoa", "San Pedro Sula", "Villanueva"
            ));
            municipiosPorDepartamento.put("El Paraíso", Arrays.asList(
                    "Alauca", "Danlí", "El Paraíso", "Güinope", "Jacaleapa", "Liure", "Morocelí",
                    "Oropolí", "Potrerillos", "San Antonio de Flores", "San Lucas", "San Matías",
                    "Soledad", "Teupasenti", "Texiguat", "Trojes", "Vado Ancho", "Yauyupe", "Yuscarán"
            ));
            municipiosPorDepartamento.put("Francisco Morazán", Arrays.asList(
                    "Alubarén", "Cantarranas", "Cedros", "Curarén", "Distrito Central", "El Porvenir",
                    "Guaimaca", "La Libertad", "La Venta", "Lepaterique", "Maraita", "Marale",
                    "Nueva Armenia", "Ojojona", "Orica", "Reitoca", "Sabanagrande",
                    "San Antonio de Oriente", "San Buenaventura", "San Ignacio", "San Miguelito",
                    "Santa Ana", "Santa Lucía", "Talanga", "Tatumbla", "Valle de Ángeles",
                    "Villa de San Francisco", "Vallecillo"
            ));
            municipiosPorDepartamento.put("Gracias a Dios", Arrays.asList(
                    "Ahuas", "Brus Laguna", "Juan Francisco Bulnes", "Puerto Lempira",
                    "Villeda Morales", "Wampusirpe"
            ));
            municipiosPorDepartamento.put("Intibucá", Arrays.asList(
                    "Camasca", "Colomoncagua", "Concepción", "Dolores", "Intibucá", "Jesús de Otoro",
                    "La Esperanza", "Magdalena", "Masaguara", "San Antonio", "San Francisco de Opalaca",
                    "San Isidro", "San Juan", "San Marcos de la Sierra", "San Miguel Guancapla",
                    "Santa Lucía", "Yamaranguila"
            ));
            municipiosPorDepartamento.put("Islas de la Bahía", Arrays.asList(
                    "Guanaja", "José Santos Guardiola", "Roatán", "Utila"
            ));
            municipiosPorDepartamento.put("La Paz", Arrays.asList(
                    "Aguanqueterique", "Cabañas", "Cane", "Chinacla", "Guajiquiro", "La Paz",
                    "Lauterique", "Marcala", "Mercedes de Oriente", "Opatoro", "San Antonio del Norte",
                    "San José", "San Juan", "San Pedro de Tutule", "Santa Ana", "Santa Elena",
                    "Santa María", "Santiago de Puringla", "Yarula"
            ));
            municipiosPorDepartamento.put("Lempira", Arrays.asList(
                    "Belén", "Candelaria", "Cololaca", "Erandique", "Gracias", "Gualcince", "Guarita",
                    "La Campa", "La Iguala", "La Unión", "La Virtud", "Las Flores", "Lepaera",
                    "Mapulaca", "Piraera", "San Andrés", "San Francisco", "San Juan Guarita",
                    "San Manuel Colohete", "San Rafael", "San Sebastián", "Santa Cruz", "Talgua",
                    "Tambla", "Tomalá", "Valladolid", "Virginia"
            ));
            municipiosPorDepartamento.put("Ocotepeque", Arrays.asList(
                    "Belén Gualcho", "Concepción", "Dolores Merendón", "Fraternidad", "La Encarnación",
                    "La Labor", "Lucerna", "Mercedes", "Ocotepeque", "San Fernando",
                    "San Francisco del Valle", "San Jorge", "San Marcos", "Santa Fe", "Sensenti", "Sinuapa"
            ));
            municipiosPorDepartamento.put("Olancho", Arrays.asList(
                    "Campamento", "Catacamas", "Concordia", "Dulce Nombre de Culmí", "El Rosario",
                    "Esquipulas del Norte", "Gualaco", "Guarizama", "Guata", "Guayape", "Jano",
                    "Juticalpa", "La Unión", "Mangulile", "Manto", "Patuca", "Salamá",
                    "San Esteban", "San Francisco de Becerra", "San Francisco de la Paz",
                    "Santa María del Real", "Silca", "Yocón"
            ));
            municipiosPorDepartamento.put("Santa Bárbara", Arrays.asList(
                    "Arada", "Atima", "Azacualpa", "Ceguaca", "Chinda", "Concepción del Norte",
                    "Concepción del Sur", "El Níspero", "Gualala", "Ilama", "Las Vegas", "Macuelizo",
                    "Naranjito", "Nueva Frontera", "Nuevo Celilac", "Petoa", "Protección", "Quimistán",
                    "San Francisco de Ojuera", "San José de las Colinas", "San Luis", "San Marcos",
                    "San Nicolás", "San Pedro Zacapa", "San Vicente Centenario", "Santa Bárbara",
                    "Santa Rita", "Trinidad"
            ));
            municipiosPorDepartamento.put("Valle", Arrays.asList(
                    "Alianza", "Amapala", "Aramecina", "Caridad", "Goascorán", "Langue", "Nacaome",
                    "San Francisco de Coray", "San Lorenzo"
            ));
            municipiosPorDepartamento.put("Yoro", Arrays.asList(
                    "Arenal", "El Negrito", "El Progreso", "Jocón", "Morazán", "Olanchito",
                    "Santa Rita", "Sulaco", "Victoria", "Yoro", "Yorito"
            ));
        }
        return municipiosPorDepartamento;
    }

    public static List<String> getMunicipios(String departamento) {
        return getMunicipiosPorDepartamento().get(departamento);
    }
} 