/*
 * Copyright 2013 Boyle Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.boylesoftware.web.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.mail.Session;
import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.boylesoftware.web.AbstractWebApplication;
import com.boylesoftware.web.ApplicationServices;
import com.boylesoftware.web.api.Authenticator;
import com.boylesoftware.web.api.FlashAttributes;
import com.boylesoftware.web.api.Model;
import com.boylesoftware.web.api.RequestParam;
import com.boylesoftware.web.api.RouteURI;
import com.boylesoftware.web.api.RouteURIBuilder;
import com.boylesoftware.web.api.Routes;
import com.boylesoftware.web.api.UserInput;
import com.boylesoftware.web.api.UserInputErrors;
import com.boylesoftware.web.spi.ControllerMethodArgHandler;
import com.boylesoftware.web.spi.ControllerMethodArgHandlerProvider;
import com.boylesoftware.web.spi.RouterRequest;
import com.boylesoftware.web.util.pool.FastPool;
import com.boylesoftware.web.util.pool.PoolableObjectFactory;


/**
 * Standard implementation of {@link ControllerMethodArgHandlerProvider} that
 * provides handlers for all the standard controller method argument types and
 * annotations.
 *
 * @author Lev Himmelfarb
 */
public class StandardControllerMethodArgHandlerProvider
	implements ControllerMethodArgHandlerProvider {

	/**
	 * Individual handler type providers.
	 */
	private final List<ControllerMethodArgHandlerProvider> providers;


	/**
	 * Create new provider.
	 *
	 * @param appServices Application services.
	 */
	public StandardControllerMethodArgHandlerProvider(
			final ApplicationServices appServices) {

		this.providers = new ArrayList<>(15);

		/*
		 * HTTP request
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos) {

				if (!paramType.equals(HttpServletRequest.class))
					return null;

				return new ControllerMethodArgHandler() {

					@Override
					public boolean usesEntityManager() {

						return false;
					}

					@Override
					public Object getArgValue(
							final RouterRequest request,
							final EntityManager em) {

						return request;
					}

					@Override
					public void onComplete(final RouterRequest request) {

						// nothing
					}
				};
			}
		});

		/*
		 * HTTP response
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos) {

				if (!paramType.equals(HttpServletResponse.class))
					return null;

				return new ControllerMethodArgHandler() {

					@Override
					public boolean usesEntityManager() {

						return false;
					}

					@Override
					public Object getArgValue(
							final RouterRequest request,
							final EntityManager em) {

						return request.getResponse();
					}

					@Override
					public void onComplete(final RouterRequest request) {

						// nothing
					}
				};
			}
		});

		/*
		 * Web-application
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos) {

				if (!(AbstractWebApplication.class).isAssignableFrom(
						paramType))
					return null;

				return new ControllerMethodArgHandler() {

					@Override
					public boolean usesEntityManager() {

						return false;
					}

					@Override
					public Object getArgValue(
							final RouterRequest request,
							final EntityManager em) {

						return appServices.getApplication();
					}

					@Override
					public void onComplete(final RouterRequest request) {

						// nothing
					}
				};
			}
		});

		/*
		 * Entity manager
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos) {

				if (!paramType.equals(EntityManager.class))
					return null;

				return new ControllerMethodArgHandler() {

					@Override
					public boolean usesEntityManager() {

						return true;
					}

					@Override
					public Object getArgValue(
							final RouterRequest request,
							final EntityManager em) {

						return em;
					}

					@Override
					public void onComplete(final RouterRequest request) {

						// nothing
					}
				};
			}
		});

		/*
		 * Authenticator.
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos) {

				if (!(Authenticator.class).isAssignableFrom(paramType))
					return null;

				return new ControllerMethodArgHandler() {

					@Override
					public boolean usesEntityManager() {

						return false;
					}

					@Override
					public Object getArgValue(
							final RouterRequest request,
							final EntityManager em) {

						return request.getAuthenticator();
					}

					@Override
					public void onComplete(final RouterRequest request) {

						// nothing
					}
				};
			}
		});

		/*
		 * User locale.
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos) {

				if (!paramType.equals(Locale.class))
					return null;

				return new ControllerMethodArgHandler() {

					@Override
					public boolean usesEntityManager() {

						return false;
					}

					@Override
					public Object getArgValue(
							final RouterRequest request,
							final EntityManager em) {

						return request.getUserLocale();
					}

					@Override
					public void onComplete(final RouterRequest request) {

						// nothing
					}
				};
			}
		});

		/*
		 * Flash attributes.
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos) {

				if (!paramType.equals(FlashAttributes.class))
					return null;

				return new ControllerMethodArgHandler() {

					@Override
					public boolean usesEntityManager() {

						return false;
					}

					@Override
					public Object getArgValue(
							final RouterRequest request,
							final EntityManager em) {

						return request.getFlashAttributes();
					}

					@Override
					public void onComplete(final RouterRequest request) {

						// nothing
					}
				};
			}
		});

		/*
		 * Routes API.
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos) {

				if (!paramType.equals(Routes.class))
					return null;

				return new ControllerMethodArgHandler() {

					@Override
					public boolean usesEntityManager() {

						return false;
					}

					@Override
					public Object getArgValue(
							final RouterRequest request,
							final EntityManager em) {

						return appServices.getApplication().getRoutes();
					}

					@Override
					public void onComplete(final RouterRequest request) {

						// nothing
					}
				};
			}
		});

		/*
		 * Route URI.
		 */
		final FastPool<RouteURIBuilderImpl> routeURIBuildersPool =
			new FastPool<>(new PoolableObjectFactory<RouteURIBuilderImpl>() {
				@Override
				public RouteURIBuilderImpl makeNew(
						final FastPool<RouteURIBuilderImpl> pool,
						final int pooledObjectId) {

					return new RouteURIBuilderImpl(pool, pooledObjectId,
							appServices.getApplication().getRoutes());
				}
			}, "RouteURIBuildersPool");
		final String pooledRouteURIBuilderAttName =
			(StandardControllerMethodArgHandlerProvider.class).getName() +
			".POOLED_ROUTE_URI_BUILDER";
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos) {

				if (paramType.equals(RouteURIBuilder.class)) {

					for (final Annotation anno : paramAnnos) {
						if (anno.annotationType().equals(RouteURI.class))
							return new ControllerMethodArgHandler() {

								@Override
								public boolean usesEntityManager() {

									return false;
								}

								@Override
								public Object getArgValue(
										final RouterRequest request,
										final EntityManager em) {

									final RouteURIBuilderImpl builder =
										routeURIBuildersPool.getSync();
									request.setAttribute(
											pooledRouteURIBuilderAttName,
											builder);
									builder.init(request,
											((RouteURI) anno).value(),
											((RouteURI) anno).type());
									return builder;
								}

								@Override
								public void onComplete(
										final RouterRequest request) {

									final RouteURIBuilderImpl builder =
										(RouteURIBuilderImpl) request
											.getAttribute(
												pooledRouteURIBuilderAttName);
									if (builder != null) {
										request.removeAttribute(
												pooledRouteURIBuilderAttName);
										builder.recycle();
									}
								}
							};
					}

				} else if (paramType.equals(String.class)) {

					for (final Annotation anno : paramAnnos) {
						if (anno.annotationType().equals(RouteURI.class))
							return new ControllerMethodArgHandler() {

								@Override
								public boolean usesEntityManager() {

									return false;
								}

								@Override
								public Object getArgValue(
										final RouterRequest request,
										final EntityManager em) {

									final Routes routes =
										appServices.getApplication()
										.getRoutes();
									return routes.getRouteURI(request,
											((RouteURI) anno).value(),
											((RouteURI) anno).type());
								}

								@Override
								public void onComplete(
										final RouterRequest request) {

									// nothing
								}
							};
					}
				}

				return null;
			}
		});

		/*
		 * JavaMail session.
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos)
				throws UnavailableException {

				if (!paramType.equals(Session.class))
					return null;

				if (appServices.getMailSession() == null)
					throw new UnavailableException(
							"JavaMail session is not configured in the JNDI.");

				return new ControllerMethodArgHandler() {

					@Override
					public boolean usesEntityManager() {

						return false;
					}

					@Override
					public Object getArgValue(
							final RouterRequest request,
							final EntityManager em) {

						return appServices.getMailSession();
					}

					@Override
					public void onComplete(final RouterRequest request) {

						// nothing
					}
				};
			}
		});

		/*
		 * Single-value request parameter.
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos) {

				if (!paramType.equals(String.class))
					return null;

				for (final Annotation anno : paramAnnos) {
					if (anno.annotationType().equals(RequestParam.class))
						return new ControllerMethodArgHandler() {

							@Override
							public boolean usesEntityManager() {

								return false;
							}

							@Override
							public Object getArgValue(
									final RouterRequest request,
									final EntityManager em) {

								return request.getParameter(
										((RequestParam) anno).value());
							}

							@Override
							public void onComplete(
									final RouterRequest request) {

								// nothing
							}
						};
				}

				return null;
			}
		});

		/*
		 * Multi-value request parameter.
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos) {

				if (!paramType.isArray() ||
						!paramType.getComponentType().equals(String.class))
					return null;

				for (final Annotation anno : paramAnnos) {
					if (anno.annotationType().equals(RequestParam.class))
						return new ControllerMethodArgHandler() {

							@Override
							public boolean usesEntityManager() {

								return false;
							}

							@Override
							public Object getArgValue(
									final RouterRequest request,
									final EntityManager em) {

								return request.getParameterValues(
										((RequestParam) anno).value());
							}

							@Override
							public void onComplete(
									final RouterRequest request) {

								// nothing
							}
						};
				}

				return null;
			}
		});

		/*
		 * Model component.
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos) {

				for (final Annotation anno : paramAnnos) {
					if (anno.annotationType().equals(Model.class))
						return new ControllerMethodArgHandler() {

						@Override
						public boolean usesEntityManager() {

							return false;
						}

						@Override
						public Object getArgValue(
								final RouterRequest request,
								final EntityManager em) {

							return request.getAttribute
									(((Model) anno).value());
						}

						@Override
						public void onComplete(
								final RouterRequest request) {

							// nothing
						}
					};
				}

				return null;
			}
		});

		/*
		 * User input bean.
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos)
				throws UnavailableException {

				for (final Annotation anno : paramAnnos) {
					if (anno.annotationType().equals(UserInput.class))
						return new UserInputControllerMethodArgHandler(
								appServices.getValidatorFactory(), paramType,
								((UserInput) anno).groups());
				}

				return null;
			}
		});

		/*
		 * User input validation errors.
		 */
		this.providers.add(new ControllerMethodArgHandlerProvider() {

			@Override
			public ControllerMethodArgHandler getHandler(
					final ServletContext sc, final int paramInd,
					final Class<?> paramType, final Annotation[] paramAnnos) {

				if (!paramType.equals(UserInputErrors.class))
					return null;

				return new ControllerMethodArgHandler() {

					@Override
					public boolean usesEntityManager() {

						return false;
					}

					@Override
					public Object getArgValue(
							final RouterRequest request,
							final EntityManager em) {

						return request.getUserInputErrors();
					}

					@Override
					public void onComplete(final RouterRequest request) {

						// nothing
					}
				};
			}
		});
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ControllerMethodArgHandlerProvider#getHandler(javax.servlet.ServletContext, int, java.lang.Class, java.lang.annotation.Annotation[])
	 */
	@Override
	public ControllerMethodArgHandler getHandler(final ServletContext sc,
			final int paramInd, final Class<?> paramType,
			final Annotation[] paramAnnos)
		throws UnavailableException {

		for (final ControllerMethodArgHandlerProvider p : this.providers) {
			final ControllerMethodArgHandler h =
				p.getHandler(sc, paramInd, paramType, paramAnnos);
			if (h != null)
				return h;
		}

		return null;
	}
}
