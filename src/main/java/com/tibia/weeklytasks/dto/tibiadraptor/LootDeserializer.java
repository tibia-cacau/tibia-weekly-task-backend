package com.tibia.weeklytasks.dto.tibiadraptor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootDeserializer extends JsonDeserializer<Map<String, List<LootItemDto>>> {

    @Override
    public Map<String, List<LootItemDto>> deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        JsonToken currentToken = parser.currentToken();

        // Se for um array vazio, retorna map vazio
        if (currentToken == JsonToken.START_ARRAY) {
            parser.skipChildren();
            return new HashMap<>();
        }

        // Se for um objeto, faz a deserialização normal
        if (currentToken == JsonToken.START_OBJECT) {
            ObjectMapper mapper = (ObjectMapper) parser.getCodec();
            TypeFactory typeFactory = mapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(
                    HashMap.class,
                    typeFactory.constructType(String.class),
                    typeFactory.constructCollectionType(List.class, LootItemDto.class));
            return mapper.readValue(parser, mapType);
        }

        // Caso contrário, retorna map vazio
        return new HashMap<>();
    }
}
