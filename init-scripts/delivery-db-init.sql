DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'delivery') THEN
        CREATE DATABASE delivery WITH OWNER deliveryapp;
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'deliveryapp') THEN
        CREATE ROLE deliveryapp WITH LOGIN PASSWORD 'deliverypassword';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.role_table_grants 
                    WHERE grantee = 'deliveryapp' AND table_schema = 'public' AND privilege_type = 'CREATE') THEN
        GRANT CREATE ON SCHEMA public TO deliveryapp;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.role_table_grants 
                    WHERE grantee = 'deliveryapp' AND table_schema = 'public' AND privilege_type = 'ALL') THEN
        GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO deliveryapp;
    END IF;
END $$;