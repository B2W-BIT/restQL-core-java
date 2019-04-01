package restql.core;

import restql.core.config.ConfigRepository;
import restql.core.config.RouteMap;
import restql.core.interop.ClojureRestQLApi;
import restql.core.query.QueryInterpolator;
import restql.core.query.QueryOptions;
import restql.core.response.QueryResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RestQL {

	/**
	 * The query options
	 */
	private QueryOptions queryOptions;

	/**
	 * restQL configurations
	 */
	private ConfigRepository configRepository;

	/**
	 * Query encoders
	 */
	private Map<String, Class> encoders = new HashMap<>();

	/**
	 * Class constructor with query options set to a production environment.
	 *
	 * @param configRepository {@link ConfigRepository}
	 */
	public RestQL(ConfigRepository configRepository) {
		this.configRepository = configRepository;
		this.queryOptions = new QueryOptions();

		// Production default to false
		this.queryOptions.setDebugging(false);
	}

	/**
	 * Class constructor with custom query options.
	 *
	 * @param configRepository {@link ConfigRepository}
	 * @param queryOptions     {@link QueryOptions}
	 */
	public RestQL(ConfigRepository configRepository, QueryOptions queryOptions) {
		this.configRepository = configRepository;
		this.queryOptions = queryOptions;
	}

	public QueryResponse executeQuery(String query, RouteMap mappings, QueryOptions queryOptions, Object... args) {
		return new QueryResponse(ClojureRestQLApi.query(mappings.toMap(),
				this.encoders,
				QueryInterpolator.interpolate(query, args),
				queryOptions != null ? queryOptions.toMap() : null));
	}

	public QueryResponse executeQuery(String query, QueryOptions queryOptions, Object... args) {
		return this.executeQuery(
				query,
				this.configRepository.getMappings(),
				queryOptions,
				args
		);
	}

	public QueryResponse executeQuery(String query, Object... args) {
		return this.executeQuery(query, this.queryOptions, args);
	}

	public void executeQueryAsync(String query,
								  RouteMap mappings,
								  QueryOptions queryOptions,
								  Consumer<QueryResponse> consumer,
								  Object... args) {

		ClojureRestQLApi.queryAsync(mappings.toMap(),
				this.encoders,
				QueryInterpolator.interpolate(query, args),
				queryOptions != null ? queryOptions.toMap() : null,
				result -> consumer.accept(new QueryResponse((String) result)));
	}

	public void executeQueryAsync(String query, QueryOptions queryOptions, Consumer<QueryResponse> consumer, Object... args) {
		this.executeQueryAsync(
				query,
				configRepository.getMappings(),
				queryOptions,
				consumer,
				args
		);
	}

	public void executeQueryAsync(String query, Consumer<QueryResponse> consumer, Object... args) {
		this.executeQueryAsync(query, this.queryOptions, consumer, args);
	}

	public <T> void setEncoder(String name, Class<T> clazz) {
		this.encoders.put(name, clazz);
	}
}
