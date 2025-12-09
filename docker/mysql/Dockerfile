FROM mysql:8.0

# 复制初始化 SQL 文件到容器
COPY init/*.sql /docker-entrypoint-initdb.d/

# 设置权限
RUN chmod -R 755 /docker-entrypoint-initdb.d/
